<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>Invoice #${invoiceNumber}</title>
    <style>
        @page {
            size: A4 ${pageOrientation!"portrait"};
            margin: 2cm;
        }

        body {
            font-family: 'Arial', sans-serif;
            color: #333;
        }

        .header {
            display: flex;
            justify-content: space-between;
            margin-bottom: 30px;
        }

        .company-info h1 {
            color: #2c3e50;
            margin: 0;
            font-size: 2em;
        }

        .invoice-info {
            text-align: right;
        }

        .invoice-info h2 {
            color: #e74c3c;
            margin: 0;
            font-size: 1.5em;
        }

        .details {
            display: flex;
            justify-content: space-between;
            margin-bottom: 30px;
        }

        .box {
            background-color: #f8f9fa;
            padding: 15px;
            border-radius: 5px;
            width: 45%;
        }

        .box h3 {
            margin-top: 0;
            color: #2c3e50;
            border-bottom: 2px solid #3498db;
            padding-bottom: 5px;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            margin: 20px 0;
        }

        table thead {
            background-color: #2c3e50;
            color: white;
        }

        table th, table td {
            padding: 12px;
            text-align: left;
            border-bottom: 1px solid #ddd;
        }

        table th {
            font-weight: bold;
        }

        table td.amount {
            text-align: right;
        }

        .totals {
            float: right;
            width: 300px;
            margin-top: 20px;
        }

        .totals table {
            margin: 0;
        }

        .totals .total-row {
            font-weight: bold;
            font-size: 1.2em;
            background-color: #e8f5e9;
        }

        .footer {
            clear: both;
            margin-top: 60px;
            padding-top: 20px;
            border-top: 2px solid #ddd;
            text-align: center;
            font-size: 0.9em;
            color: #7f8c8d;
        }
    </style>
</head>
<body>
    <div class="header">
        <div class="company-info">
            <h1>${companyName}</h1>
            <p>${companyAddress}</p>
            <p>${companyCity}, ${companyState} ${companyZip}</p>
            <p>Phone: ${companyPhone}</p>
        </div>
        <div class="invoice-info">
            <h2>INVOICE</h2>
            <p><strong>Invoice #:</strong> ${invoiceNumber}</p>
            <p><strong>Date:</strong> ${invoiceDate}</p>
            <#if dueDate??>
                <p><strong>Due Date:</strong> ${dueDate}</p>
            </#if>
        </div>
    </div>

    <div class="details">
        <div class="box">
            <h3>Bill To:</h3>
            <p><strong>${customerName}</strong></p>
            <p>${customerAddress}</p>
            <p>${customerCity}, ${customerState} ${customerZip}</p>
            <#if customerEmail??>
                <p>Email: ${customerEmail}</p>
            </#if>
        </div>

        <div class="box">
            <h3>Invoice Details:</h3>
            <#if purchaseOrder??>
                <p><strong>PO Number:</strong> ${purchaseOrder}</p>
            </#if>
            <#if terms??>
                <p><strong>Terms:</strong> ${terms}</p>
            </#if>
            <p><strong>Status:</strong> ${status!"Pending"}</p>
        </div>
    </div>

    <table>
        <thead>
            <tr>
                <th style="width: 10%;">Item #</th>
                <th style="width: 40%;">Description</th>
                <th style="width: 15%;">Quantity</th>
                <th style="width: 15%;">Unit Price</th>
                <th style="width: 20%;" class="amount">Amount</th>
            </tr>
        </thead>
        <tbody>
            <#list lineItems as item>
                <tr>
                    <td>${item?counter}</td>
                    <td>${item.description}</td>
                    <td>${item.quantity}</td>
                    <td>$${item.unitPrice?string["0.00"]}</td>
                    <td class="amount">$${(item.quantity * item.unitPrice)?string["0.00"]}</td>
                </tr>
            </#list>
        </tbody>
    </table>

    <div class="totals">
        <table>
            <tr>
                <td><strong>Subtotal:</strong></td>
                <td class="amount">$${subtotal?string["0.00"]}</td>
            </tr>
            <#if discount?? && discount gt 0>
                <tr>
                    <td><strong>Discount:</strong></td>
                    <td class="amount">-$${discount?string["0.00"]}</td>
                </tr>
            </#if>
            <#if tax?? && tax gt 0>
                <tr>
                    <td><strong>Tax (${taxRate}%):</strong></td>
                    <td class="amount">$${tax?string["0.00"]}</td>
                </tr>
            </#if>
            <tr class="total-row">
                <td><strong>TOTAL:</strong></td>
                <td class="amount"><strong>$${total?string["0.00"]}</strong></td>
            </tr>
        </table>
    </div>

    <div class="footer">
        <#if notes??>
            <p style="text-align: left; margin-bottom: 20px;"><strong>Notes:</strong> ${notes}</p>
        </#if>
        <p>Thank you for your business!</p>
        <p>Please remit payment to the address above.</p>
    </div>
</body>
</html>

