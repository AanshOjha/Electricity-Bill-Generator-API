# Electricity Bill Generator
This is a Spring Boot Rest API which uses MySQL.

## [Demo Video](https://1drv.ms/v/s!Agwf35R6wYp7gbJRGvHbgTXyHPILPA?e=7UeiqX)

## Features:
* Registers user and give them a unique meter ID.
* User can insert their current month readings, **previous month reading are fetched automatically from DB**.
* User can generate Electricity bill PDF for the month specified, by entering meter ID and date.
* User can delete their account also by specifying their meter ID in the link. Eg. 'localhost:8080/bill/deleteuser?meterId=4554`.

## API Endpoints used:
### **POST**: `localhost:8080/bill/register`

```json
{
    "name":"Aansh",
    "address": "Address, India",
    "password": "password",
    "email": "example@test.com"
}
```

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

### **GET**: `localhost:8080/bill/getallusers`

### **DELETE**: `localhost:8080/bill/deleteuser?meterId=4554`

### **DELETE**: `localhost:8080/bill/deleteall`
