# Data Seeding Quick Start Script
# This PowerShell script demonstrates how to use the data seeding endpoints

# Configuration
$baseUrl = "http://localhost:8080"
$adminToken = "YOUR_ADMIN_JWT_TOKEN_HERE"

# Headers for requests
$headers = @{
    "Authorization" = "Bearer $adminToken"
    "Content-Type" = "application/json"
}

Write-Host "🌱 Electricity Bill Generator - Data Seeding Demo" -ForegroundColor Green
Write-Host "=================================================" -ForegroundColor Green

# Function to make API calls with error handling
function Invoke-ApiCall {
    param(
        [string]$Method,
        [string]$Endpoint,
        [hashtable]$Headers,
        [string]$Body = $null
    )
    
    try {
        $url = "$baseUrl$Endpoint"
        Write-Host "📡 $Method $url" -ForegroundColor Yellow
        
        if ($Body) {
            $response = Invoke-RestMethod -Uri $url -Method $Method -Headers $Headers -Body $Body
        } else {
            $response = Invoke-RestMethod -Uri $url -Method $Method -Headers $Headers
        }
        
        Write-Host "✅ Success: $($response.message -or $response)" -ForegroundColor Green
        return $response
    }
    catch {
        Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

Write-Host "`n1️⃣ Checking current test customers..." -ForegroundColor Cyan
$testCustomers = Invoke-ApiCall -Method "GET" -Endpoint "/api/admin/debug/test-customers" -Headers $headers

if ($testCustomers -and $testCustomers.customers -and $testCustomers.customers.Count -gt 0) {
    Write-Host "📋 Found existing test customers:" -ForegroundColor Blue
    foreach ($customer in $testCustomers.customers) {
        Write-Host "   • $($customer.name) ($($customer.email)) - $($customer.totalReadings) readings" -ForegroundColor Gray
    }
    
    $continue = Read-Host "`n🤔 Test customers already exist. Clear them first? (y/N)"
    if ($continue.ToLower() -eq 'y') {
        Write-Host "`n🧹 Clearing existing test data..." -ForegroundColor Cyan
        Invoke-ApiCall -Method "DELETE" -Endpoint "/api/admin/debug/clear-test-data" -Headers $headers
    }
}

Write-Host "`n2️⃣ Seeding new test data..." -ForegroundColor Cyan
$seedResult = Invoke-ApiCall -Method "POST" -Endpoint "/api/admin/debug/seed-data" -Headers $headers

if ($seedResult) {
    Write-Host "`n3️⃣ Verifying seeded data..." -ForegroundColor Cyan
    $newTestCustomers = Invoke-ApiCall -Method "GET" -Endpoint "/api/admin/debug/test-customers" -Headers $headers
    
    if ($newTestCustomers -and $newTestCustomers.customers) {
        Write-Host "`n📊 Data Seeding Summary:" -ForegroundColor Blue
        Write-Host "========================" -ForegroundColor Blue
        foreach ($customer in $newTestCustomers.customers) {
            Write-Host "👤 Customer: $($customer.name)" -ForegroundColor White
            Write-Host "   📧 Email: $($customer.email)" -ForegroundColor Gray
            Write-Host "   🔢 Customer ID: $($customer.id)" -ForegroundColor Gray
            Write-Host "   ⚡ Meter(s): $($customer.meterNumbers -join ', ')" -ForegroundColor Gray
            Write-Host "   📈 Total Readings: $($customer.totalReadings)" -ForegroundColor Gray
            Write-Host ""
        }
        
        Write-Host "🎉 Data seeding completed successfully!" -ForegroundColor Green
        Write-Host "`n💡 Next steps:" -ForegroundColor Yellow
        Write-Host "   • Use these customers to test billing features" -ForegroundColor Gray
        Write-Host "   • Generate bills using /api/admin/billing/generate" -ForegroundColor Gray
        Write-Host "   • Test customer login with password: password123" -ForegroundColor Gray
        
        Write-Host "`n🧪 Test Credentials:" -ForegroundColor Yellow
        Write-Host "   Email: user.a@example.com | Password: password123" -ForegroundColor Gray
        Write-Host "   Email: user.b@example.com | Password: password123" -ForegroundColor Gray
        Write-Host "   Email: user.c@example.com | Password: password123" -ForegroundColor Gray
    }
}

Write-Host "`n🏁 Demo completed!" -ForegroundColor Green
