
## Docker container for DB scripts

#### PostgreSQL
docker run --name postgres -d -p 5432:5432 -e POSTGRES_PASSWORD=postgres -e POSTGRES_USER=postgres postgres:alpine

#### MySQL
docker run --name mysql -e MYSQL_ROOT_PASSWORD=mysql -e MYSQL_DATABASE=mysql -e MYSQL_USER=mysql -e MYSQL_PASS=mysql -p 3306:3306 -d mysql

#### Oracle
docker run -d -p 49160:22 -p 49161:1521 -e ORACLE_ALLOW_REMOTE=true wnameless/oracle-xe-11g

#### MS-SQL
docker run -e 'ACCEPT_EULA=Y' -e 'SA_PASSWORD=mssqlserver' -p 1433:1433 -d --name mssql microsoft/mssql-server-linux:latest
