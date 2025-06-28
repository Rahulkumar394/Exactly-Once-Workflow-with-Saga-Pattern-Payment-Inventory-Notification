
Testing Guide: Starting se End Tak

Hum yeh maanke chal rahe hain ki aapne mvn clean install -DskipTests se project build kar liya hai.

Step 1: Sabhi Services ko Background me Start Karein

Sabse pehle, hum saare containers ko start karenge. -d flag use karne se yeh sab background me chalenge aur aapka terminal free rahega.
==================================================
Terminal me yeh command chalayein:

Generated bash
docker compose up -d --build

up: Containers ko start karo.
-d: Detached mode, yaani background me chalao.
--build: Agar code me koi change hai to naya Docker image banao.

Thodi der wait karein. Yeh check karne ke liye ki sabhi services chal rahi hain ya nahi, yeh command chalayein:


Expected Output (Aapke screenshot jaisa):
Aapko 7 containers dikhenge aur un sabka STATUS Up hona chahiye.

Generated code
NAME                          IMAGE                                        COMMAND                  SERVICE                     CREATED             STATUS              PORTS
inventory-service             sagaorchestratorproject-inventory-service    "java -jar /app.jar"     inventory-service           27 seconds ago      Up 25 seconds       
kafka                         confluentinc/cp-kafka:7.5.3                  "/etc/confluent/dock…"   kafka                       27 seconds ago      Up 25 seconds       0.0.0.0:9092-9093->9092-9093/tcp
notification-service          sagaorchestratorproject-notification-service "java -jar /app.jar"     notification-service        27 seconds ago      Up 25 seconds       
payment-service               sagaorchestratorproject-payment-service      "java -jar /app.jar"     payment-service             27 seconds ago      Up 25 seconds       
postgres-db                   postgres:15                                  "docker-entrypoint.s…"   postgres-db                 27 seconds ago      Up 26 seconds       0.0.0.0:5433->5432/tcp
saga-orchestrator-service     sagaorchestratorproject-saga-orchestrator-service "java -jar /app.jar"  saga-orchestrator-service   27 seconds ago      Up 25 seconds       0.0.0.0:8080->8080/tcp
zookeeper                     confluentinc/cp-zookeeper:7.5.3              "/etc/confluent/dock…"   zookeeper                   27 seconds ago      Up 26 seconds       2888/tcp, 0.0.0.0:2181->2181/tcp

=================================================
Step 2: Live Logs Dekhna Shuru Karein
Ab hum sabhi services ke live logs ek saath dekhenge taaki hum transaction ko real-time me track kar sakein.
Ek naya terminal open karein (purana terminal free hai) aur yeh command chalayein:

Generated bash
docker-compose logs -f

logs: Sabhi containers ke logs dikhao.
-f: Follow, yaani live logs dikhate raho, ruko mat.
Ab is terminal me sabhi services ke logs aane shuru ho jayenge. Isko side me khula rakhein.

==================================================

Step 3: Manual Testing Shuru Karein (Task Description ke Anusaar)

Ab ek aur naya terminal open karein (total 3 terminal ho gaye). Isme hum curl commands chalayenge.
Test Case 1: Successful Transaction
Action: Is terminal me, ek successful order ka request bhejein.

Generated bash
curl -X POST http://localhost:8080/api/orders \
-H "Content-Type: application/json" \
-d '{
    "orderId": "happy-order-001",
    "userId": "rahul-dev",
    "productId": "macbook-pro",
    "quantity": 1,
    "amount": 2500.00
}'


Output (Aapke curl wale terminal me):
Aapko turant yeh message milega:
Saga process started with ID: d9ffd71a-edc8-485e-88e1-78a0635af48e (ID alag hoga)

Output (Aapke docker-compose logs -f wale terminal me):
Ab is terminal par dhyan dein. Aapko aesa flow dikhega (har line ke aage service ka naam hoga):

saga-orchestrator-service | Saga ... started. Sent payment request.

payment-service | Saga ...: Processing payment...

saga-orchestrator-service | Saga ...: Payment successful. Requesting inventory update.

inventory-service | Saga ...: Updating inventory...

inventory-service | Saga ...: Inventory updated successfully.

saga-orchestrator-service | Saga ...: Inventory updated. Requesting notification.

notification-service | Saga ...: Sending notification...

notification-service | Saga ...: Notification sent successfully.

saga-orchestrator-service | Saga ...: Notification sent. SAGA COMPLETED SUCCESSFULLY.

Agar aapko aakhri line me "SAGA COMPLETED SUCCESSFULLY" dikhta hai, to aapka pehla test PASS ho gaya.

===========================================================

Test Case 2: Failure in Step 2 (Inventory) and Rollback

Action: Ab curl wale terminal me, ek aisa order bhejein jiska productId "FAIL-ME" ho.

Generated bash
curl -X POST http://localhost:8080/api/orders \
-H "Content-Type: application/json" \
-d '{
    "orderId": "fail-order-002",
    "userId": "test-user",
    "productId": "FAIL-ME",
    "quantity": 5,
    "amount": 10.00
}'


Output (Aapke curl wale terminal me):
Saga process started with ID: ...

Output (Aapke docker-compose logs -f wale terminal me):
Is baar flow alag hoga:

saga-orchestrator-service | Saga ... started. Sent payment request.

payment-service | Saga ...: Processing payment...

saga-orchestrator-service | Saga ...: Payment successful. Requesting inventory update.

inventory-service | Saga ...: Updating inventory...

inventory-service | Saga ...: Inventory update failed (SIMULATED). <-- Yahan fail hua!

saga-orchestrator-service | Saga ...: Inventory failed. Compensating payment. <-- Rollback shuru

payment-service | Saga ...: Compensating payment (refunding)... <-- Payment ko refund kiya

saga-orchestrator-service | Saga ...: Payment failed or compensated. Saga rolled back. <-- Rollback poora hua

Agar aapko yeh compensation/rollback wale logs dikhte hain, to aapka doosra test bhi PASS ho gaya.


========================================================
Test Case 3: Database State Verification

Action: curl wale terminal me (ya koi bhi free terminal me), database me jaakar check karein.

docker exec -it postgres-db psql -U user -d saga_db

SELECT saga_id, order_id, current_status FROM saga_state;

Expected Output:

Generated code
saga_id         |      order_id       | current_status 
------------------------+---------------------+----------------
 d9ffd71a...(id)        | happy-order-001     | COMPLETED
 c1a4b2...(id)          | fail-order-002      | ROLLED_BACK
(2 rows)


Yeh confirm karta hai ki orchestrator ka state machine aache se kaam kar raha hai.

Testing Poori Hui

Jab aapki saari testing ho jaye, to aap docker-compose logs -f wale terminal me Ctrl + C dabakar logs dekhna band kar sakte hain.

Aur jab aapko saare containers band karne hon, to yeh command chalayein: for stop all container

Generated bash
docker compose down
