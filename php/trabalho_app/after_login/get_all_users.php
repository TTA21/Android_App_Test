<?php

require "../init.php";

$sql_query = "SELECT username FROM users;";

$result = mysqli_query( $con , $sql_query );

$response  = array();

while( $row = mysqli_fetch_array($result) ){

    array_push( $response,
                array(
                    "username" => $row[0],
                    )
    );

}

echo json_encode( array("server_response" => $response ) );

mysqli_close($con);

?>