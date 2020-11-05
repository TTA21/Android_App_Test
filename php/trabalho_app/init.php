<?php

$db_name = "trabalhoM2";
$mysql_username = "root";
$mysql_password = "admin";

$server_name = "localhost";

$con = mysqli_connect( $server_name , $mysql_username , $mysql_password , $db_name );

if( !$con ){
    //echo "Conection failure";
    //echo mysqli_connect_error();
}else{
    //echo "<h3>Succesful connection</h3>";
}

?>