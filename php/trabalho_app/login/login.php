<?php

require "../init.php";


$username = $_POST["username"];
$password = $_POST["password"];

$sql_query = "SELECT id FROM users WHERE username='$username' AND password='$password'; ";

$result = mysqli_query( $con , $sql_query );

if( mysqli_num_rows( $result ) > 0 ){
    $row = mysqli_fetch_assoc($result);
    $id = $row["id"];

    echo $id;  ///this gets sent
    
}else{
    echo "Username or password incorrect";
}

mysqli_close($con);

?>