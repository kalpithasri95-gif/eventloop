# 🚀 EVENTLOOP – Method 1: 100% Free 24/7 Cloud Deployment Guide
## (Zero Host Involvement – No Laptop Needed to Stay On)

This guide shows you how to host **EventLoop** online for **FREE**, 24 hours a day, 7 days a week.
Once deployed, anyone (students, faculty, club organizers, store managers) can open your app on **mobile phones, tablets, or laptops** via a public `https://...` link.

---

## 🛠️ Step 0: Test it Locally on Your Computer

Before deploying to the cloud, you can test it locally right now:

1. In this project folder, double-click **`run_web.bat`** (or run `run_web.bat` in the terminal).
2. Open your browser and go to:
   ```
   http://localhost:8080
   ```
3. You will see the complete **EventLoop Web App** with live KPIs, Catalogue, Verification Audit, Smart Matching, Bookings, Reports, and OOP Lab!

---

## ☁️ Step-by-Step Free Cloud Deployment (Takes ~3 minutes)

### Step 1: Create Free Accounts
1. **GitHub**: [https://github.com](https://github.com) (Free)
2. **Render**: [https://render.com](https://render.com) (Free - sign in with your GitHub account)

---

### Step 2: Push EventLoop to GitHub
Open your terminal in this project folder (`c:\New folder`) and run:

```bash
git init
git add .
git commit -m "EventLoop Production Web Application"
git branch -M main
```

Create a new repository on GitHub (e.g., named `eventloop`), then connect and push:
```bash
git remote add origin https://github.com/YOUR_GITHUB_USERNAME/eventloop.git
git push -u origin main
```

---

### Step 3: Deploy for FREE on Render.com
1. Go to [https://dashboard.render.com](https://dashboard.render.com).
2. Click the **"New +"** button at top right and select **"Web Service"**.
3. Select **"Build and deploy from a Git repository"** and choose your `eventloop` repository.
4. Render will automatically detect the **`Dockerfile`** in the repository!
5. In the settings:
   - **Name**: `eventloop` (or whatever you prefer)
   - **Instance Type**: Select **"Free"** ($0.00 / month)
6. Click **"Create Web Service"**.

---

### Step 4: Done! Your 24/7 Web App is Live! 🎉
Render will build the Docker container and give you a permanent, free SSL link:
```
https://eventloop.onrender.com
```

### ✅ What this means:
- **Zero Host Involvement**: Your laptop can be completely turned off or asleep.
- **24/7 Availability**: The server runs on cloud servers continuously.
- **100% Free**: No credit card required, fits completely in the free tier.
- **Mobile Responsive**: Fully touch-friendly and formatted for Android & iOS screens.
- **Multi-User Collaboration**: Store managers and students can log in simultaneously from anywhere in the world.

---

## 📱 Alternative Free Host: Railway.app
If you prefer Railway:
1. Go to [https://railway.app](https://railway.app).
2. Click **"New Project"** -> **"Deploy from GitHub repo"**.
3. Select your `eventloop` repository.
4. Railway automatically detects `Dockerfile` and deploys it with a single click.
