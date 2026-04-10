python -m venv venv
venv\Scripts\activate
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
./mvnw spring-boot:run

py -3.11 -m venv venv311
venv311\Scripts\activate
