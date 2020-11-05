<?php

///DONE

require "../init.php";

$id = 0;
$username = $_POST["username"];
$email = $_POST["email"];
$password = $_POST["password"];

    ///check if the params have already been registered
    $sql_pre_query = "select id from users where username='$username';";
    $query_check = mysqli_query( $con , $sql_pre_query );

    if( mysqli_num_rows( $query_check ) > 0 ){  ///If username already registered, kill process
        echo "Username already registered!";
    }else{  ///If username not registered, check email

        $sql_pre_query = "select id from users where email='$email';";
        $query_check = mysqli_query( $con , $sql_pre_query );

        if( mysqli_num_rows( $query_check ) > 0 ){  ///If email already registered, kill process
            echo "Email already registered!";
        }else{  ///if none of them have been registered before, register

            $sql_query = "INSERT INTO users VALUES( '$id' , '$username' , '$email' , '$password' );"; 

            if( mysqli_query( $con , $sql_query ) ){
                echo "Succesful Registration";
            }else{
                echo "Unsuccesful Registration";
                echo mysqli_connect_error();
            }

        }

    }

mysqli_close($con);


?>