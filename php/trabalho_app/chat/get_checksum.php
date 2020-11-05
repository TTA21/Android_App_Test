<?php

require "../init.php";

$table_name = $_POST["table_name"];
//$table_name = "user1_user2_s33_m10_h19_d15_m10_y2020";

$sql_query = "CHECKSUM TABLE $table_name";

$result = mysqli_query( $con , $sql_query );

echo mysqli_fetch_array($result)[1];

mysqli_close($con);


?>