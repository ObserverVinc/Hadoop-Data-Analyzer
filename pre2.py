import pandas as pd

# Load dataset
file_path = "AdidasSales.csv"  # Update this with the path to your file
data = pd.read_csv(file_path, delimiter=';')

# Step 1: Replace delimiters for CSV standardization
data.columns = [col.strip() for col in data.columns]  # Remove leading/trailing spaces
data.replace({r'\s*;\s*': ','}, regex=True, inplace=True)

# Step 2: Clean numerical columns and convert to appropriate types
columns_to_clean = ["Price per Unit", "Total Sales", "Operating Profit"]
for col in columns_to_clean:
    data[col] = (
        data[col]
        .replace({r'\$': '', r',': '.', r'\s': ''}, regex=True)  # Remove $ and replace , with .
        .astype(float)  # Convert to float
    )

# Step 3: Clean "Units Sold" to make them integers
data["Units Sold"] = (
    data["Units Sold"]
    .replace({r'\.': ''}, regex=True)  # Remove dot notation
    .astype(int)  # Convert to integer
)

# Step 4: Remove '%' from "Operating Margin" and convert to float
data["Operating Margin"] = (
    data["Operating Margin"]
    .str.replace('%', '', regex=False)
    .astype(float)
)

# Step 5: Save the cleaned data to a new CSV file
output_path = "Processed_AdidasSales2.csv"
data.to_csv(output_path, index=False)
print(f"Data cleaned and saved to {output_path}")