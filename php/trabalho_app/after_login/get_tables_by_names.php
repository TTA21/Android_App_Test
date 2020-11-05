<?php

require "../init.php";

//$username = "user1";
//$username2 = "user2";

$username = $_POST["username1"];
$username2 = $_POST["username2"]; 

$sql_query =    "SHOW TABLES WHERE tables_in_trabalhom2 LIKE '%{$username}_{$username2}%';";

$result = mysqli_query( $con , $sql_query );

$response  = array();

while( $row = mysqli_fetch_array($result) ){

    array_push( $response,
                array(
                    "Tables_in_trabalhom2" => $row[0]
                    )
    );

}

if( empty ( $response ) ){

    $response = array();

    $sql_query =    "SHOW TABLES WHERE tables_in_trabalhom2 LIKE '%{$username2}_{$username}%';";

    $result = mysqli_query( $con , $sql_query );

    $response  = array();

    while( $row = mysqli_fetch_array($result) ){

        array_push( $response,
                    array(
                        "Tables_in_trabalhom2" => $row[0]
                        )
        );

    }

}


echo json_encode( array("server_response" => $response ) );

mysqli_close($con);

?>