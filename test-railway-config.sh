#!/bin/bash

echo "🚂 Testing Railway Configuration Locally"
echo "========================================"

# Set Railway environment variables for testing
export SPRING_PROFILES_ACTIVE=railway
export SPRING_DATASOURCE_URL="jdbc:postgresql://ep-calm-sunset-a1zvj4q6-pooler.ap-southeast-1.aws.neon.tech:5432/neondb?sslmode=require"
export SPRING_DATASOURCE_USERNAME="neondb_owner"
export SPRING_DATASOURCE_PASSWORD="npg_jc7UJPAy3CHV"
export JWT_ACCESS_KEY="QnaO9hBEdCb6nN2TY4dQqYhnDtc9FNHvfgSs2o7WzSk="
export JWT_REFRESH_KEY="lFLJoeiSFysXqkNsdda4kFSZ7c74+PJ2eVZ4ZGkZ8NA="
export GOOGLE_CLIENT_ID="1089362795193-65rtu7dqu8ec6frotre6u145drvh87cc.apps.googleusercontent.com"
export GOOGLE_CLIENT_SECRET="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"
export BREVO_API_KEY="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"
export BREVO_SENDER_EMAIL="mnam3239@gmail.com"
export BREVO_SENDER_NAME="SimpleCommerce MDM (Railway Test)"
export CLOUDINARY_CLOUD_NAME="dqhzwvfl8"
export CLOUDINARY_API_KEY="688358942523463"
export CLOUDINARY_API_SECRET="JpFtS7ff7ng5zoQacYJrhejdbEg"
export FRONTEND_URL="http://localhost:5173"

# Business configuration
export BUSINESS_EMAIL_CONFIRMATION_DEADLINE_HOURS=24
export BUSINESS_EMAIL_DELIVERY_ESTIMATION_DAYS=3
export BUSINESS_SOCIAL_FACEBOOK_URL="https://facebook.com/simplecommerce"
export BUSINESS_SOCIAL_INSTAGRAM_URL="https://instagram.com/simplecommerce"
export BUSINESS_COMPANY_WEBSITE_URL="https://simplecommerce.com"
export BUSINESS_SUPPORT_EMAIL="support@simplecommerce.com"
export BUSINESS_SUPPORT_PHONE="+84-xxx-xxx-xxx"
export BUSINESS_SHIPPING_FREE_THRESHOLD=500000
export BUSINESS_SHIPPING_DEFAULT_FEE=30000
export BUSINESS_SHIPPING_DEFAULT_CARRIER="Viettel Post"
export BUSINESS_COMMISSION_ADMIN_RATE=0.05
export BUSINESS_COMMISSION_PAYMENT_GATEWAY_RATE=0.025
export BUSINESS_ORDER_STOCK_UPDATE_MAX_RETRIES=3

# JVM options for 1GB RAM
export JAVA_OPTS="-Xmx768m -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+OptimizeStringConcat -XX:MaxRAMPercentage=75.0"

echo "✅ Environment variables set for Railway testing"
echo "📊 JVM Memory: Max 768MB, Initial 256MB"
echo "🗄️ Database: Neon PostgreSQL (online)"
echo "🔧 Profile: railway"
echo ""

echo "🚀 To start the application with Railway config:"
echo "   mvn spring-boot:run"
echo ""
echo "🧪 To test configuration:"
echo "   1. Start the application"
echo "   2. Check logs for business configuration loading"
echo "   3. Test health endpoint: curl http://localhost:8080/actuator/health"
echo "   4. Test business config API: curl http://localhost:8080/api/v1/admin/business-config"
echo ""
echo "💡 Memory monitoring commands:"
echo "   - Check JVM memory: jcmd \$(pgrep java) VM.info"
echo "   - Monitor GC: jcmd \$(pgrep java) GC.run_finalization"
