$env:DB_URL="jdbc:postgresql://ep-.../neondb?sslmode=require"
$env:DB_USERNAME="neondb_owner"
$env:DB_PASSWORD="npg_XXXX"

Get-ChildItem Env:

./mvnw spring-boot:run

mvn clean install -DskipTests