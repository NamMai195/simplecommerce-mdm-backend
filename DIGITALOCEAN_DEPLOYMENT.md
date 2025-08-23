# 🌊 DigitalOcean Deployment Guide

## 🎯 Overview
Deploy SimpleCommerce MDM Backend trên DigitalOcean Droplet 1GB RAM với Docker.

## 💰 Cost Estimate
- **Droplet**: $6/month (1GB RAM, 1 vCPU, 25GB SSD)
- **Domain** (optional): $12/year
- **Total**: ~$6-7/month

---

## 📋 Prerequisites

### 1. DigitalOcean Account
- Tạo account tại [DigitalOcean](https://www.digitalocean.com/)
- Add payment method
- Get $200 credit với student account

### 2. External Services
- **Cloudinary**: Image storage ([cloudinary.com](https://cloudinary.com/))
- **Brevo**: Email service ([brevo.com](https://brevo.com/))

---

## 🚀 Deployment Steps

### Step 1: Tạo DigitalOcean Droplet

1. **Login** DigitalOcean Dashboard
2. **Create Droplet**:
   - **Image**: Ubuntu 22.04 LTS
   - **Size**: Basic ($6/month, 1GB RAM, 1 vCPU)
   - **Datacenter**: Singapore (gần VN nhất)
   - **Authentication**: SSH Key (recommended) or Password
   - **Hostname**: simplecommerce-prod

3. **Wait** droplet được tạo (~2 minutes)

### Step 2: Setup Server

```bash
# SSH vào server
ssh root@your-droplet-ip

# Update system
apt update && apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# Install Docker Compose
apt install docker-compose-plugin -y

# Verify installation
docker --version
docker compose version
```

### Step 3: Setup Application

```bash
# Tạo thư mục app
mkdir -p /opt/simplecommerce
cd /opt/simplecommerce

# Clone repository
git clone https://github.com/NamMai195/simplecommerce-mdm-backend.git .
git checkout deploy/nam/digitalocean

# Copy environment file
cp digitalocean.env.example .env

# Edit environment variables
nano .env
```

### Step 4: Configure Environment Variables

Edit `.env` file với values thực tế:

```bash
# Database - Tạo strong password
POSTGRES_PASSWORD=super_strong_password_123!

# JWT - Generate random 32+ characters
JWT_SECRET=your_jwt_secret_key_32_characters_minimum

# Brevo (Email service)
BREVO_API_KEY=your_brevo_api_key
BREVO_SENDER_EMAIL=noreply@yourdomain.com

# Cloudinary (Image service)
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
```

### Step 5: Deploy Application

```bash
# Build và start services
docker compose -f docker-compose.digitalocean.yml up -d --build

# Check logs
docker compose -f docker-compose.digitalocean.yml logs -f

# Verify services
docker compose -f docker-compose.digitalocean.yml ps
```

### Step 6: Setup Firewall

```bash
# Enable UFW firewall
ufw enable

# Allow SSH
ufw allow ssh

# Allow HTTP/HTTPS
ufw allow 80
ufw allow 443

# Allow app port
ufw allow 8080

# Check status
ufw status
```

---

## 🔧 Management Commands

### Check Application Status
```bash
# Service status
docker compose -f docker-compose.digitalocean.yml ps

# Application logs
docker compose -f docker-compose.digitalocean.yml logs app

# Database logs
docker compose -f docker-compose.digitalocean.yml logs postgres
```

### Update Application
```bash
cd /opt/simplecommerce

# Pull latest code
git pull origin deploy/nam/digitalocean

# Rebuild và restart
docker compose -f docker-compose.digitalocean.yml up -d --build
```

### Backup Database
```bash
# Create backup
docker compose -f docker-compose.digitalocean.yml exec postgres pg_dump -U simplecommerce simplecommerce_mdm > backup_$(date +%Y%m%d).sql

# Restore backup
docker compose -f docker-compose.digitalocean.yml exec -T postgres psql -U simplecommerce simplecommerce_mdm < backup_20240824.sql
```

---

## 🌐 Access URLs

- **API**: `http://your-droplet-ip:8080`
- **Swagger**: `http://your-droplet-ip:8080/swagger-ui.html`
- **Health Check**: `http://your-droplet-ip:8080/actuator/health`

---

## 🐛 Troubleshooting

### Application Won't Start
```bash
# Check memory usage
free -h

# Check logs
docker compose -f docker-compose.digitalocean.yml logs app

# Restart services
docker compose -f docker-compose.digitalocean.yml restart
```

### Out of Memory
```bash
# Check container memory
docker stats

# Reduce memory limits in docker-compose.digitalocean.yml
# app: memory limit 512M → 400M
# postgres: memory limit 256M → 200M
```

### Database Connection Issues
```bash
# Check postgres logs
docker compose -f docker-compose.digitalocean.yml logs postgres

# Reset database
docker compose -f docker-compose.digitalocean.yml down
docker volume rm simplecommerce_postgres_data
docker compose -f docker-compose.digitalocean.yml up -d
```

---

## 📊 Performance Monitoring

### Check Resource Usage
```bash
# Server resources
htop

# Docker stats
docker stats

# Disk usage
df -h
```

### Application Metrics
- **Startup Time**: ~60-90 seconds
- **Memory Usage**: ~500-600MB
- **Response Time**: 1-3 seconds
- **Concurrent Users**: 10-20 users

---

## 🔒 Security Best Practices

1. **Change default passwords**
2. **Setup SSH key authentication**
3. **Regular security updates**
4. **Monitor logs for suspicious activity**
5. **Backup data regularly**

---

## 💡 Optimization Tips

### For Better Performance
1. **Add swap**: 1GB swap file
2. **Optimize JVM**: Tune garbage collection
3. **Database tuning**: Optimize PostgreSQL config
4. **Enable gzip**: Compress responses

### For Cost Saving
1. **Schedule downtime**: Stop services when not needed
2. **Monitor usage**: DigitalOcean monitoring
3. **Optimize images**: Use smaller Docker images

---

## 📞 Support

- **Documentation**: This guide
- **Issues**: GitHub repository issues
- **DigitalOcean Docs**: [DigitalOcean Community](https://docs.digitalocean.com/)

---

**🎉 Happy Deploying!**
