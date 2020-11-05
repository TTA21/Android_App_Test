<?php

require "../init.php";

$today = getdate();

$message_time = "{$today["hours"]}:{$today["minutes"]}:{$today["seconds"]}";
$username = $_POST["user"];    
$message = $_POST["message"];  
$table_name = $_POST["table_name"];
$type_of_message = $_POST["type"];

$sql_query = "INSERT INTO $table_name VALUES(
                                                0,
                                                '$username',
                                                '$message_time',
                                                '$message',
                                                '$type_of_message'
                                            );";


if( mysqli_query( $con , $sql_query ) ){
    echo "Message Sent";
}else{
    echo mysqli_connect_error();
}

?>