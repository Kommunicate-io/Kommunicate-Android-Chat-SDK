## Migrating to 2.7.0

- We have migrated multiple package name. If you face any error during build, please remove old imports and import the Kommunicate reference again, for correct import.
- If you use custom layout by overriding our layouts, upgrade the layout by taking reference from our Github repo
    "mobicom_message_list" 
- Few strings have been renamed. If you use these strings, change it in your strings.xml file:
    "applozic_my_profile_option_info" -> "kommunicate_my_profile_option_info"
    "applozic_select_photo" -> "kommunicate_select_photo"
    "applozic_network_usage" -> "kommunicate_network_usage"
- Rename "applozic-settings.json" to "kommunicate-settings.json"