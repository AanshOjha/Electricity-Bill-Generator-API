

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

### **DELETE**: `localhost:8080/bill/deleteuser?meterId=4554`

### **DELETE**: `localhost:8080/bill/deleteall`

### **POST**: `localhost:8080/bill/insertreading`

```json
{
  "password": "12345",
  "meterId" : 2956,
  "currentMonthReading": 143.89
}
```

### **GET**: `localhost:8080/bill/pdf`

```json
{
  "date": "2024-06",
  "meterId": 2956,
  "password": "12345"
}
```

### **DELETE**: `localhost:8080/bill/deletereading`

```json
{
    "meterId": 3952,
    "password": "12345",
    "date": "2024-07"
}
```


