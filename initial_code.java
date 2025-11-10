import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
from sklearn.preprocessing import StandardScaler
from sklearn.cluster import KMeans
from sklearn.linear_model import LogisticRegression, LinearRegression
from sklearn.neighbors import KNeighborsClassifier
from sklearn.metrics import silhouette_score, accuracy_score, confusion_matrix, r2_score
from sklearn.decomposition import PCA
from sklearn.model_selection import train_test_split

file_path = r"C:\Users\kousi\Downloads\shopping_behavior_updated.csv"
df = pd.read_csv(file_path)
df.columns = df.columns.str.strip()
df = df.drop_duplicates()

column_map = {
    'Purchase Amount USD': None,
    'Review Rating': None,
    'Previous Purchases': None,
    'Promo Code Used': None,
    'Category': None,
    'Location': None,
    'Season': None,
    'Customer ID': None
}

for col in df.columns:
    col_lower = col.lower().replace(" ", "")
    if 'purchase' in col_lower and 'usd' in col_lower:
        column_map['Purchase Amount USD'] = col
    elif 'review' in col_lower and 'rating' in col_lower:
        column_map['Review Rating'] = col
    elif 'previous' in col_lower and 'purchase' in col_lower:
        column_map['Previous Purchases'] = col
    elif 'promo' in col_lower and 'code' in col_lower:
        column_map['Promo Code Used'] = col
    elif 'category' in col_lower:
        column_map['Category'] = col
    elif 'location' in col_lower:
        column_map['Location'] = col
    elif 'season' in col_lower:
        column_map['Season'] = col
    elif 'customer' in col_lower and 'id' in col_lower:
        column_map['Customer ID'] = col

df[column_map['Purchase Amount USD']] = pd.to_numeric(df[column_map['Purchase Amount USD']], errors='coerce')
df[column_map['Review Rating']] = pd.to_numeric(df[column_map['Review Rating']], errors='coerce')
df[column_map['Previous Purchases']] = pd.to_numeric(df[column_map['Previous Purchases']], errors='coerce')
df = df.dropna(subset=[column_map['Purchase Amount USD'], column_map['Review Rating'], column_map['Previous Purchases']])

X = df[[column_map['Purchase Amount USD'], column_map['Review Rating'], column_map['Previous Purchases']]]
scaler = StandardScaler()
X_scaled = scaler.fit_transform(X)

kmeans = KMeans(n_clusters=3, random_state=123, n_init=5)
df['Segment'] = kmeans.fit_predict(X_scaled)
print("Silhouette Score:", silhouette_score(X_scaled, df['Segment']))

df['AvgPurchasePerVisit'] = df[column_map['Purchase Amount USD']] / (df[column_map['Previous Purchases']] + 1)
X_reg = df[['Review Rating', 'Previous Purchases', 'AvgPurchasePerVisit']]
y_reg = df[column_map['Purchase Amount USD']]

lin_reg = LinearRegression()
lin_reg.fit(X_reg, y_reg)
pred_lin = lin_reg.predict(X_reg)
print("Linear Regression Coefficients:", lin_reg.coef_)
print("Linear Regression Intercept:", lin_reg.intercept_)
print("Linear Regression R² Score:", r2_score(y_reg, pred_lin))

plt.figure(figsize=(8,6))
plt.scatter(y_reg, pred_lin, alpha=0.6, color='purple')
plt.plot([y_reg.min(), y_reg.max()], [y_reg.min(), y_reg.max()], 'r--')
plt.xlabel("Actual Purchase Amount")
plt.ylabel("Predicted Purchase Amount")
plt.title("Linear Regression: Actual vs Predicted")
plt.show()

y = df[column_map['Promo Code Used']].apply(lambda x: 1 if str(x).strip().lower() == "yes" else 0)

X_train_lr, X_test_lr, y_train_lr, y_test_lr, train_idx, test_idx = train_test_split(
    X_scaled, y, np.arange(len(df)), test_size=0.2, random_state=123
)

log_reg = LogisticRegression(max_iter=200)
log_reg.fit(X_train_lr, y_train_lr)
pred_logreg_train = log_reg.predict(X_train_lr)
pred_logreg_test = log_reg.predict(X_test_lr)

print("Logistic Regression Train Accuracy:", accuracy_score(y_train_lr, pred_logreg_train))
print("Logistic Regression Test Accuracy:", accuracy_score(y_test_lr, pred_logreg_test))

cm_log = confusion_matrix(y_test_lr, pred_logreg_test)
plt.figure(figsize=(5,4))
sns.heatmap(cm_log, annot=True, fmt='d', cmap='Blues')
plt.title("Logistic Regression Confusion Matrix (Test Set)")
plt.xlabel("Predicted")
plt.ylabel("Actual")
plt.show()

promo_customers = df.iloc[test_idx][pred_logreg_test == 1][column_map['Customer ID']].nunique()
print("Promo Code Likely Customers (Test Set):", promo_customers)

median_spend = df[column_map['Purchase Amount USD']].median()
df['HighSpender'] = df[column_map['Purchase Amount USD']].apply(lambda x: 1 if x > median_spend else 0)

X_train, X_test, y_train, y_test = train_test_split(
    X_scaled, df['HighSpender'], test_size=0.2, random_state=123
)

knn = KNeighborsClassifier(n_neighbors=7)
knn.fit(X_train, y_train)
y_pred_knn = knn.predict(X_test)

print("KNN Accuracy (High vs Low Spenders):", accuracy_score(y_test, y_pred_knn))

cm_knn = confusion_matrix(y_test, y_pred_knn)
plt.figure(figsize=(5,4))
sns.heatmap(cm_knn, annot=True, fmt='d', cmap='Oranges')
plt.title("KNN Confusion Matrix (High vs Low Spenders)")
plt.xlabel("Predicted")
plt.ylabel("Actual")
plt.show()

pca = PCA(n_components=2)
pca_result = pca.fit_transform(X_scaled)

plt.figure(figsize=(8,6))
plt.scatter(pca_result[:,0], pca_result[:,1], c=df['Segment'], cmap='rainbow', alpha=0.6)
plt.title('Customer Segments by PCA', fontsize=14)
plt.xlabel('PCA Component 1')
plt.ylabel('PCA Component 2')
plt.colorbar(label='Segment')
plt.show()

df['Measurement'] = 'Ratio'
df['DataType'] = 'Structured'

segment_freq = df['Segment'].value_counts()
most_freq_segment = segment_freq.idxmax()
print("Most common customer type (segment):", most_freq_segment)
print("Most bought item category:", df[column_map['Category']].mode()[0])
print("Top shopping location:", df[column_map['Location']].mode()[0])

df.to_csv('complete_shopping_analysis_ordered_models.csv', index=False)
print("Analysis complete. File saved as 'complete_shopping_analysis_ordered_models.csv'")

