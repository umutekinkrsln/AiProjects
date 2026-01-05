import pandas as pd
import torch
import torch.nn as nn
import torch.optim as optim
import matplotlib.pyplot as plt
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder

# 1. CSV oku
data = pd.read_csv("squat_data.csv")

# angle sütununu sayıya çevir (string varsa float yapar)
data["angle"] = pd.to_numeric(data["angle"], errors="coerce")
data = data.dropna(subset=["angle"])  # NaN varsa temizle

X = data["angle"].values.reshape(-1, 1).astype("float32")
y = LabelEncoder().fit_transform(data["label"])

# 2. Train/test ayır
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2)

# 3. Tensorlara çevir
X_train = torch.tensor(X_train, dtype=torch.float32)
y_train = torch.tensor(y_train, dtype=torch.long)
X_test = torch.tensor(X_test, dtype=torch.float32)
y_test = torch.tensor(y_test, dtype=torch.long)

# 4. Basit MLP modeli
class Net(nn.Module):
    def __init__(self):
        super(Net, self).__init__()
        self.fc1 = nn.Linear(1, 16)
        self.fc2 = nn.Linear(16, 3)  # 3 sınıf: Standing, Squat, HalfSquat
    def forward(self, x):
        x = torch.relu(self.fc1(x))
        return self.fc2(x)

net = Net()
criterion = nn.CrossEntropyLoss()
optimizer = optim.Adam(net.parameters(), lr=0.01)

# 5. Eğitim döngüsü
losses, accs = [], []
for epoch in range(20):
    optimizer.zero_grad()
    outputs = net(X_train)
    loss = criterion(outputs, y_train)
    loss.backward()
    optimizer.step()
    losses.append(loss.item())

    _, predicted = torch.max(outputs, 1)
    acc = (predicted == y_train).sum().item() / len(y_train)
    accs.append(acc)

    print(f"Epoch {epoch+1}, Loss: {loss.item():.4f}, Accuracy: {acc:.2f}")

# 6. Grafik
plt.figure(figsize=(8,4))
plt.plot(losses, label="Loss", color="red")
plt.plot(accs, label="Accuracy", color="blue")
plt.title("Squat Classifier Training")
plt.xlabel("Epoch")
plt.ylabel("Value")
plt.legend()
plt.show()
