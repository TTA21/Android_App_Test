<?php

require "../init.php";

///Used to make the converstion tables where chat takes place

$today = getdate();

$conversation_table_id = "s{$today["seconds"]}_m{$today["minutes"]}_h{$today["hours"]}_D{$today["mday"]}_M{$today["mon"]}_Y{$today["year"]}";
$username_1 = $_POST["user1"];    ///the one who created
$username_2 = $_POST["user2"];  ///the one who joined

//$username_1 = "user1";    ///the one who created
//$username_2 = "user2";  ///the one who joined

$sql_query = "CREATE TABLE {$username_1}_{$username_2}_{$conversation_table_id} (
                                                                                    id INT(10) PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                                                                    line_author VARCHAR(255) NOT NULL,
                                                                                    time_of_writing VARCHAR(255) NOT NULL,
                                                                                    written LONGTEXT NOT NULL,
                                                                                    type_of_message VARCHAR(255) NOT NULL
                                                                                );";


if( mysqli_query( $con , $sql_query ) ){
    echo "{$username_1}_{$username_2}_{$conversation_table_id}";
}else{
    echo mysqli_connect_error();
}

mysqli_close($con);

?>