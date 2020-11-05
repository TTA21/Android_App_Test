<?php

require "../init.php";

$table_name = $_POST["table_name"];

$sql_query = "SELECT * FROM $table_name";

$result = mysqli_query( $con , $sql_query );

$response  = array();

while( $row = mysqli_fetch_array($result) ){

    array_push( $response,
                array(
                    "line_author" => $row[1],
                    "time_of_writing" => $row[2],
                    "written" => $row[3],
                    "type" => $row[4]
                    )
    );

}

echo json_encode( array("server_response" => $response ) );

mysqli_close($con);

?>