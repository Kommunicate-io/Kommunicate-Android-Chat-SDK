## Migrating to 2.6.6

- We have changed our formData format to match with the Web SDK. If you were using webhooks/inline code earlier with form submission, please check this migration guide:

- Previous versions:
    "formData" was stringified twice. To get data, you will have to parse the Message string and then "formData" key again.

- Current version:
    formData is not stringified. Parsing the whole message body will give you the json value of "formData"