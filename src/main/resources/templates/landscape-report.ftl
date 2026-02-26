<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>${title!"Report"}</title>
    <style>
        @page {
            size: A4 ${pageOrientation!"portrait"};
            margin: 1.5cm;
        }

        body {
            font-family: 'Arial', sans-serif;
            color: #333;
            margin: 0;
            padding: 0;
        }

        h1 {
            color: #2c3e50;
            text-align: center;
            border-bottom: 2px solid #3498db;
            padding-bottom: 10px;
            margin-bottom: 20px;
            page-break-after: avoid;
        }

        h2 {
            color: #34495e;
            border-left: 4px solid #3498db;
            padding-left: 10px;
            margin-top: 20px;
            margin-bottom: 15px;
            page-break-after: avoid;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            margin: 15px 0;
            font-size: 0.95em;
        }

        table thead {
            background-color: #3498db;
            color: white;
        }

        table th, table td {
            padding: 10px;
            text-align: left;
            border: 1px solid #ddd;
        }

        table tbody tr:nth-child(even) {
            background-color: #f9f9f9;
        }

        table tbody tr:hover {
            background-color: #f5f5f5;
        }

        .header {
            text-align: center;
            margin-bottom: 25px;
            page-break-after: avoid;
        }

        .info-section {
            background-color: #ecf0f1;
            padding: 12px;
            border-radius: 4px;
            margin-bottom: 15px;
            page-break-inside: avoid;
        }

        .info-section p {
            margin: 5px 0;
            font-size: 0.95em;
        }

        .label {
            font-weight: bold;
            color: #2c3e50;
        }

        .summary {
            background-color: #e8f5e9;
            padding: 12px;
            border-left: 4px solid #4caf50;
            margin: 15px 0;
            page-break-inside: avoid;
        }

        .footer {
            margin-top: 30px;
            padding-top: 15px;
            border-top: 1px solid #ddd;
            text-align: center;
            font-size: 0.85em;
            color: #7f8c8d;
            page-break-inside: avoid;
        }

        .two-column {
            display: table;
            width: 100%;
        }

        .column {
            display: table-cell;
            width: 48%;
            padding-right: 2%;
            vertical-align: top;
        }

        .column:last-child {
            padding-right: 0;
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>${title!"Report"}</h1>
        <#if subtitle??>
            <p style="font-size: 1.1em; color: #7f8c8d; margin: 10px 0 0 0;">${subtitle}</p>
        </#if>
    </div>

    <#if generatedDate?? || author?? || description??>
        <div class="info-section">
            <#if generatedDate??>
                <p><span class="label">Generated on:</span> ${generatedDate}</p>
            </#if>
            <#if author??>
                <p><span class="label">Author:</span> ${author}</p>
            </#if>
            <#if description??>
                <p><span class="label">Description:</span> ${description}</p>
            </#if>
        </div>
    </#if>

    <#if items?? && items?size gt 0>
        <h2>Items</h2>
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Description</th>
                    <th>Quantity</th>
                    <th>Price</th>
                </tr>
            </thead>
            <tbody>
                <#list items as item>
                    <tr>
                        <td>${item.id}</td>
                        <td>${item.name}</td>
                        <td>${item.description!"N/A"}</td>
                        <td style="text-align: center;">${item.quantity}</td>
                        <td style="text-align: right;">$${item.price?string["0.00"]}</td>
                    </tr>
                </#list>
            </tbody>
        </table>

        <#if total??>
            <div class="summary">
                <p style="font-size: 1.1em; margin: 0;">
                    <span class="label">Total:</span> $${total?string["0.00"]}
                </p>
            </div>
        </#if>
    </#if>

    <#if sections?? && sections?size gt 0>
        <#list sections as section>
            <h2>${section.title}</h2>
            <p>${section.content}</p>
        </#list>
    </#if>

    <#if dataTable?? && dataTable?size gt 0>
        <h2>Data Table</h2>
        <table>
            <tbody>
                <#list dataTable as row>
                    <tr>
                        <#list row as cell>
                            <td>${cell}</td>
                        </#list>
                    </tr>
                </#list>
            </tbody>
        </table>
    </#if>

    <div class="footer">
        <p>This report was automatically generated by Jambock Reports Engine</p>
        <#if pageOrientation??>
            <p>Orientation: <strong>${pageOrientation}</strong></p>
        </#if>
    </div>
</body>
</html>

