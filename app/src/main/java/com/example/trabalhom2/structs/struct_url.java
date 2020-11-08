package com.example.trabalhom2.structs;

public class struct_url {

    private String address = "http://192.168.25.4:0080/trabalho_app/";      ///LocalHost

    private String login_url =                  address + "login/login.php";

    private String register_url =               address + "register/register.php";

    private String aft_log_get_all_users =      address + "after_login/get_all_users.php";
    private String aft_log_search_conv_tables = address + "after_login/get_tables_by_names.php";
    private String aft_log_create_conv_table =  address + "after_login/make_conversation_table.php";

    private String chat_get_conversations_url = address + "chat/get_conversation.php";
    private String chat_get_checksum =          address + "chat/get_checksum.php";
    private String chat_send_message =          address + "chat/send_message.php";



    public String getLogin_url() {
        return login_url;
    }

    public String getRegister_url() {
        return register_url;
    }

    public String getAft_log_get_all_users() {
        return aft_log_get_all_users;
    }
    public String getAft_log_search_conv_tables() {
        return aft_log_search_conv_tables;
    }
    public String getAft_log_create_conv_table() {
        return aft_log_create_conv_table;
    }

    public String getChat_get_conversations_url() {
        return chat_get_conversations_url;
    }
    public String getChat_get_checksum() {
        return chat_get_checksum;
    }
    public String getChat_send_message() {
        return chat_send_message;
    }
}
