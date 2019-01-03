# preprocessor
A preprocessor of Stack Overflow dump to perform stemming, remove stop words, generate synonyms for tags and extract code blocks in table posts.


### Prerequisites

Softwares:
1. [Java 1.8] 
2. [Postgres 9.3]. Configure your DB to accept local connections. An example of *pg_hba.conf* configuration:

```
...
# TYPE  DATABASE        USER            ADDRESS                 METHOD
# "local" is for Unix domain socket connections only
local   all             all                                     md5
# IPv4 local connections:
host    all             all             127.0.0.1/32            md5
...
```
3. [PgAdmin] (we used PgAdmin 3) but feel free to use any DB tool for PostgreSQL. 

4. [Maven 3](https://maven.apache.org/)

### Installing the app.
1. Download the [SO Dump of March 2017](http://lascam.facom.ufu.br/companion/duplicatequestion/backup_so_2017_raw_basic_tables_ok.backup) containing the original content downloaded from [SO Official Dump](https://archive.org/details/stackexchange). 

2. On your DB tool, create a new database named stackoverflow2017. This is a query example:
```
CREATE DATABASE stackoverflow2017
  WITH OWNER = postgres
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'en_US.UTF-8'
       LC_CTYPE = 'en_US.UTF-8'
       CONNECTION LIMIT = -1;
```
3. Restore the downloaded dump to the created database. 

Obs: restoring this dump would require at least 100 Gb of free space. If your operating system runs in a partition with insufficient free space, create a tablespace pointing to a larger partition and associate the database to it by replacing the "TABLESPACE" value to the new tablespace name: `TABLESPACE = tablespacename`. 

4. Assert the database is sound. Execute the following SQL command: `select title,body,tags,tagssyn,code  from posts where title is not null limit 10`. The return should list the main fields for 10 posts. Here, the column "body" should contains special html tags like `<p>`.  

5. Assert Maven is correctly installed. In a Terminal enter with the command: `mvn --version`. This should return the version of Maven. 

## Running the process

1. Edit the *application.properties* file under *src/main/resources/* and set the your data_base password parameter. The file comes with default values for performing stemming and removing stop words. You need to fill only variables: `spring.datasource.password=YOUR_DB_PASSWORD`. Change `spring.datasource.username` if your db user is not postgres. 

2. In a terminal, go to the Project_folder and build the jar file with the Maven command: `mvn package -Dmaven.test.skip=true`. Assert that preprocessor.jar is built under target folder. 

3. Go to Project_folder/target and run the command to execute the jar: `java -Xms1024M -Xmx20g -jar ./preprocessor.jar`. 


### Results

The logs are displayed in the terminal but you can check if the process ended successfully by running the following SQL on your DB tool: `select title,body,tags,tagssyn,code  from posts where title is not null limit 10`. Observe that title and body fields are stemmed and had the stop words removed. Also the tagssyn and code columns were filled.


## Authors

* Rodrigo Fernandes  - *Initial work* - [Muldon](https://github.com/muldon)
* Klerisson Paixao - [Klerisson](http://klerisson.github.io/)
* Marcelo Maia - [Marcelo](http://buscatextual.cnpq.br/buscatextual/visualizacv.do?id=K4791753E8)


## License
This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details



[work]: https://soarsmu.github.io/papers/jcst-duplicateqns.pdf
[Java 1.8]: http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html
[Mallet]: http://mallet.cs.umass.edu/
[Postgres 9.3]: https://www.postgresql.org/download/
[PgAdmin]: https://www.pgadmin.org/download/
[Dump of March 2017]: http://lapes.ufu.br/so/
