Put this on your server folder and change the ip adress on the structs class.
 
 DO NOT INCLUDE THE ACTUAL "PHP" parent folder, just the "trabalho_app".

Also change the variables on the "init.php" to match your database

The schema generation and table handling is done enntirely by the php scripts.

The project needs a schema called : trabalhom2
The scema needs a table called "users" wich is made of:
+----------+--------------+------+-----+---------+----------------+
| Field    | Type         | Null | Key | Default | Extra          |
+----------+--------------+------+-----+---------+----------------+
| id       | int          | NO   | PRI | NULL    | auto_increment |
| username | varchar(255) | NO   |     | NULL    |                |
| email    | varchar(255) | NO   |     | NULL    |                |
| password | varchar(255) | NO   |     | NULL    |                |
+----------+--------------+------+-----+---------+----------------+

