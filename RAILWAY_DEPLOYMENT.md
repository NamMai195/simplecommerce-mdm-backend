# 🚀 Railway Deployment Guide

## 📋 Prerequisites
- Railway account (free tier)
- GitHub repository connected
- PostgreSQL database (Railway provides)

## 🎯 Deployment Steps

### 1. Setup Railway Account
```
1. Go to https://railway.app
2. Sign up with GitHub
3. Create new project
4. Choose "Deploy from GitHub repo"
```

### 2. Connect Repository
```
1. Select your repository: simplecommerce-mdm-backend
2. Choose branch: test/nam/railway-deployment
3. Railway will auto-detect Spring Boot
```

### 3. Configure Environment Variables
```
Copy from railway.env.example and fill in:
- DATABASE_URL (Railway PostgreSQL)
- JWT_SECRET_KEY
- BREVO_API_KEY
- CLOUDINARY_* keys
```

### 4. Deploy
```
1. Railway will auto-build
2. Build time: 3-5 phút
3. Deploy time: 1-2 phút
4. Total: 5-7 phút
```

## 🔧 Configuration Details

### Memory Optimization
```
JVM: -Xmx256m -Xms128m
Connection Pool: 5 connections
Thread Pool: 2-4 threads
Logging: WARN level only
```

### Database
```
Railway PostgreSQL (included)
Connection limit: 5
Auto-scaling: Yes
Backup: Automatic
```

## 📊 Performance Expectations

### Free Tier (0.5GB RAM + 1 CPU)
```
API Response: 1-3 giây
Database: 500ms - 2 giây
Concurrent Users: 5-10
Stability: 80-85%
```

### If Upgrade to $5/month
```
API Response: 200-500ms
Database: 100-400ms
Concurrent Users: 50-100
Stability: 99.9%
```

## 🧪 Testing Checklist

### Basic Functionality
- [ ] Health check endpoint
- [ ] Database connection
- [ ] JWT authentication
- [ ] Basic CRUD APIs

### Advanced Features
- [ ] File upload (Cloudinary)
- [ ] Email service (Brevo)
- [ ] Admin dashboard
- [ ] Seller dashboard

### Performance Test
- [ ] Response time < 3 giây
- [ ] Memory usage < 256MB
- [ ] No OutOfMemoryError
- [ ] Stable under load

## 🚨 Troubleshooting

### Common Issues
1. **OutOfMemoryError**: Upgrade to $5/month
2. **Build Failed**: Check Java version (17)
3. **Database Connection**: Verify DATABASE_URL
4. **JWT Error**: Check secret keys

### Rollback Plan
```
If free tier fails:
1. Upgrade to $5/month
2. Test performance
3. Decide deployment strategy
```

## 💰 Cost Analysis

### Free Tier
```
Cost: $0/month
Performance: Basic
Suitable: Testing only
```

### Paid Tier
```
Cost: $5/month
Performance: Production ready
Suitable: Real deployment
```

## 🎯 Next Steps

1. **Deploy to Railway Free**
2. **Test all functionality**
3. **Monitor performance**
4. **Decide upgrade path**
5. **Merge to main if successful**
