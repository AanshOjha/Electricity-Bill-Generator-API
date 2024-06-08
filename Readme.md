### **POST**: `localhost:8080/bill/register`

```json
{
"name":"Aansh",
"address": "Address, India",
"password": "password",
"email": "example@test.com"
}
```

### **GET**: `localhost:8080/bill/getallusers`

### **DELETE**: `localhost:8080/bill/deleteuser?meter_id=4554`

### **DELETE**: `localhost:8080/bill/deleteall`

### **POST**: `localhost:8080/bill/insertreading`

```json
{
    "meterId" : 5953,
    "currentMonthReading": 666.89
}
```

### **GET**: `localhost:8080/bill/pdf`

```json
{
    "date": "2024-06-08",
    "meterId": 5953
}
```


