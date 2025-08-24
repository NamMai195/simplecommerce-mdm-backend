# 🚂 Railway Deployment Guide
## SimpleCommerce MDM Backend

### 📋 Prerequisites
- Railway.app account
- GitHub repository connected to Railway
- Online PostgreSQL database (using existing Neon database)
- 1GB RAM / 1 vCPU Railway plan

### 🚀 Deployment Steps

#### 1. **Create Railway Project**
```bash
# Install Railway CLI
npm install -g @railway/cli

# Login to Railway
railway login

# Create new project
railway init
```

#### 2. **Database Configuration**
- **Option A (Recommended)**: Use existing Neon online database (already configured)
- **Option B**: Add Railway PostgreSQL service if you prefer Railway-managed database
- The configuration is set to use Neon database by default

#### 3. **Configure Environment Variables**
Add these variables in Railway dashboard (Variables section):

```bash
# Application Configuration
SPRING_PROFILES_ACTIVE=railway

# Database - Neon PostgreSQL (Online Database)
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-calm-sunset-a1zvj4q6-pooler.ap-southeast-1.aws.neon.tech:5432/neondb?sslmode=require
SPRING_DATASOURCE_USERNAME=neondb_owner
SPRING_DATASOURCE_PASSWORD=npg_jc7UJPAy3CHV

# Authentication
JWT_ACCESS_KEY=QnaO9hBEdCb6nN2TY4dQqYhnDtc9FNHvfgSs2o7WzSk=
JWT_REFRESH_KEY=lFLJoeiSFysXqkNsdda4kFSZ7c74+PJ2eVZ4ZGkZ8NA=

# Google OAuth2
GOOGLE_CLIENT_ID=1089362795193-65rtu7dqu8ec6frotre6u145drvh87cc.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=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

# Email Service (Brevo)
BREVO_API_KEY=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
BREVO_SENDER_EMAIL=mnam3239@gmail.com
BREVO_SENDER_NAME=SimpleCommerce MDM (Production)

# File Storage (Cloudinary)
CLOUDINARY_CLOUD_NAME=dqhzwvfl8
CLOUDINARY_API_KEY=688358942523463
CLOUDINARY_API_SECRET=JpFtS7ff7ng5zoQacYJrhejdbEg

# Frontend URL
FRONTEND_URL=https://simplecommerce-frontend.up.railway.app

# Business Logic Configuration
BUSINESS_EMAIL_CONFIRMATION_DEADLINE_HOURS=24
BUSINESS_EMAIL_DELIVERY_ESTIMATION_DAYS=3
BUSINESS_SOCIAL_FACEBOOK_URL=https://facebook.com/simplecommerce
BUSINESS_SOCIAL_INSTAGRAM_URL=https://instagram.com/simplecommerce
BUSINESS_COMPANY_WEBSITE_URL=https://simplecommerce.com
BUSINESS_SUPPORT_EMAIL=support@simplecommerce.com
BUSINESS_SUPPORT_PHONE=+84-xxx-xxx-xxx
BUSINESS_SHIPPING_FREE_THRESHOLD=500000
BUSINESS_SHIPPING_DEFAULT_FEE=30000
BUSINESS_SHIPPING_DEFAULT_CARRIER=Viettel Post
BUSINESS_COMMISSION_ADMIN_RATE=0.05
BUSINESS_COMMISSION_PAYMENT_GATEWAY_RATE=0.025
BUSINESS_ORDER_STOCK_UPDATE_MAX_RETRIES=3
```

#### 4. **Deploy to Railway**
```bash
# Deploy current branch
railway up

# Or connect GitHub repository for automatic deployments
# Railway will detect changes and auto-deploy
```

### 🔧 Configuration Files

#### **railway.toml**
```toml
[build]
builder = "NIXPACKS"

[deploy]
healthcheckPath = "/actuator/health"
healthcheckTimeout = 300
restartPolicyType = "ON_FAILURE"
restartPolicyMaxRetries = 10
```

#### **application-railway.yml**
- Optimized for Railway deployment
- Uses `DATABASE_URL` environment variable
- Configured for PostgreSQL
- Disabled H2 console for security
- Business logic configurations via environment variables

### 🌐 API Endpoints

Once deployed, your API will be available at:
```
https://your-app-name.up.railway.app
```

#### **Health Check**
```
GET https://your-app-name.up.railway.app/actuator/health
```

#### **API Documentation**
```
GET https://your-app-name.up.railway.app/swagger-ui.html
```

#### **Business Configuration**
```
GET https://your-app-name.up.railway.app/api/v1/admin/business-config
```

### 📊 Monitoring

#### **Railway Dashboard**
- View deployment logs
- Monitor resource usage
- Check application metrics

#### **Application Logs**
```bash
# View logs via Railway CLI
railway logs

# Follow logs in real-time
railway logs --follow
```

#### **Business Configuration Logs**
Look for startup logs showing loaded business configuration:
```
🚀 ================================================================================================
📊 BUSINESS CONFIGURATION LOADED - SimpleCommerce MDM Backend
🚀 ================================================================================================
📧 EMAIL BUSINESS RULES:
   ⏰ Confirmation Deadline: 24 hours
🚚 SHIPPING & LOGISTICS RULES:
   💰 Free Shipping Threshold: 500000 VND
💰 COMMISSION & REVENUE RULES:
   🏛️ Admin Commission Rate: 5% (0.05 decimal)
✅ All Business Configuration Variables Successfully Loaded!
```

### 🔐 Security Considerations

✅ **All sensitive data in environment variables**  
✅ **H2 console disabled in production**  
✅ **PostgreSQL with connection pooling**  
✅ **Auto-commit disabled for transaction safety**  
✅ **Optimized logging levels for production**  

### 💰 Cost Estimation

**Railway Starter Plan (Recommended):**
- $5/month
- 1GB RAM / 1 vCPU
- Suitable for production use
- Includes $5 monthly usage credit

**Configuration Optimizations for 1GB RAM:**
- JVM heap: Max 768MB, Initial 256MB
- Database connections: Max 3, Min 1
- G1 Garbage Collector for better memory management
- Connection pooling optimized for limited resources

### 🎯 Next Steps

1. **Deploy and test** the application
2. **Monitor** application logs for business configuration
3. **Test** API endpoints and business logic
4. **Update** frontend URL once frontend is deployed
5. **Configure** custom domain if needed

### 🆘 Troubleshooting

#### **Common Issues:**
- **Database connection**: Verify `DATABASE_URL` is set
- **Environment variables**: Check all required vars are set
- **Memory issues**: Monitor RAM usage, upgrade if needed
- **Business config**: Check startup logs for configuration validation

#### **Support Resources:**
- Railway Documentation: https://docs.railway.app
- Railway Discord: https://discord.gg/railway
- Application logs: `railway logs`
