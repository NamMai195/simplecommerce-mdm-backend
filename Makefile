# ==============================================
# SimpleCommerce MDM Backend - Makefile
# ==============================================

# Load environment variables from .env file
ifneq (,$(wildcard ./.env))
    include .env
    export
endif

.PHONY: help build run stop clean test dev prod logs services-up services-down

# Default goal
.DEFAULT_GOAL := help

# Variables
APP_NAME ?= simplecommerce-mdm
COMPOSE_PROJECT_NAME ?= simplecommerce
VERSION := $(shell git describe --tags --always --dirty 2>/dev/null || echo "latest")
DOCKER_IMAGE := $(APP_NAME):$(VERSION)

# Colors for terminal output
RED := \033[0;31m
GREEN := \033[0;32m
YELLOW := \033[0;33m
BLUE := \033[0;34m
NC := \033[0m # No Color

help: ## Show this help message
	@echo "$(BLUE)SimpleCommerce MDM Backend - Available Commands$(NC)"
	@echo "=================================================="
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "$(GREEN)%-20s$(NC) %s\n", $$1, $$2}' $(MAKEFILE_LIST)

# ==============================================
# Development Commands
# ==============================================

dev: ## Start development environment (requires .env file)
	@if [ ! -f .env ]; then \
		echo "$(RED)Error: .env file not found. Please copy env.example to .env and configure it.$(NC)"; \
		exit 1; \
	fi
	@echo "$(YELLOW)Starting development environment...$(NC)"
	docker-compose up --build -d
	@echo "$(GREEN)Development environment started!$(NC)"
	@echo "$(BLUE)Application: http://localhost:$(SERVER_PORT)$(NC)"
	@echo "$(BLUE)pgAdmin: http://localhost:$(PGADMIN_PORT)$(NC)"

dev-logs: ## Show development logs
	docker-compose logs -f api

dev-stop: ## Stop development environment
	@echo "$(YELLOW)Stopping development environment...$(NC)"
	docker-compose down
	@echo "$(GREEN)Development environment stopped!$(NC)"

dev-clean: ## Clean development environment (remove volumes)
	@echo "$(RED)Cleaning development environment...$(NC)"
	docker-compose down -v --remove-orphans
	docker system prune -f
	@echo "$(GREEN)Development environment cleaned!$(NC)"

# ==============================================
# Podman Commands (for rootless containers)
# ==============================================

podman-dev: ## Start development environment with Podman
	@if [ ! -f .env ]; then \
		echo "$(RED)Error: .env file not found. Please copy env.example to .env and configure it.$(NC)"; \
		exit 1; \
	fi
	@echo "$(YELLOW)Starting development environment with Podman...$(NC)"
	podman-compose -f podman-compose.yml up --build -d
	@echo "$(GREEN)Podman environment started!$(NC)"
	@echo "$(BLUE)Application: http://localhost:$(SERVER_PORT)$(NC)"
	@echo "$(BLUE)pgAdmin: http://localhost:$(PGADMIN_PORT)$(NC)"

podman-logs: ## Show Podman development logs
	podman-compose -f podman-compose.yml logs -f api

podman-stop: ## Stop Podman development environment
	@echo "$(YELLOW)Stopping Podman environment...$(NC)"
	podman-compose -f podman-compose.yml down
	@echo "$(GREEN)Podman environment stopped!$(NC)"

podman-clean: ## Clean Podman environment (remove volumes)
	@echo "$(RED)Cleaning Podman environment...$(NC)"
	podman-compose -f podman-compose.yml down -v
	@echo "$(GREEN)Podman environment cleaned!$(NC)"

podman-services-up: ## Start only background services with Podman
	@echo "$(YELLOW)Starting background services with Podman...$(NC)"
	podman-compose -f podman-compose.yml up -d db redis pgadmin

podman-services-down: ## Stop background services with Podman
	@echo "$(YELLOW)Stopping background services with Podman..."
	podman-compose -f podman-compose.yml down

podman-db-connect: ## Connect to Podman development database using credentials from .env
	@echo "$(YELLOW)Connecting to Podman development database...$(NC)"
	podman exec -it ${COMPOSE_PROJECT_NAME}-db psql -U ${POSTGRES_USER} -d ${POSTGRES_DB}

# ==============================================
# Production Commands
# ==============================================

prod-build: ## Build production image
	@echo "$(YELLOW)Building production image...$(NC)"
	docker build -t $(DOCKER_IMAGE) -f Containerfile .
	@echo "$(GREEN)Production image built: $(DOCKER_IMAGE)$(NC)"

prod-up: ## Start production environment
	@echo "$(YELLOW)Starting production environment...$(NC)"
	docker-compose -f docker-compose.prod.yml up -d
	@echo "$(GREEN)Production environment started!$(NC)"

prod-stop: ## Stop production environment
	@echo "$(YELLOW)Stopping production environment...$(NC)"
	docker-compose -f docker-compose.prod.yml down
	@echo "$(GREEN)Production environment stopped!$(NC)"

prod-logs: ## Show production logs
	docker-compose -f docker-compose.prod.yml logs -f api

# ==============================================
# Maven Commands
# ==============================================

maven-clean: ## Clean Maven project
	@echo "$(YELLOW)Cleaning Maven project...$(NC)"
	./mvnw clean
	@echo "$(GREEN)Maven project cleaned!$(NC)"

maven-test: ## Run tests
	@echo "$(YELLOW)Running tests...$(NC)"
	./mvnw test
	@echo "$(GREEN)Tests completed!$(NC)"

maven-package: ## Package application
	@echo "$(YELLOW)Packaging application...$(NC)"
	./mvnw clean package -DskipTests
	@echo "$(GREEN)Application packaged!$(NC)"

maven-run: ## Run application locally (without Docker)
	@echo "$(YELLOW)Running application locally...$(NC)"
	./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# ==============================================
# Database Commands
# ==============================================

db-connect: ## Connect to development database using credentials from .env
	@echo "$(YELLOW)Connecting to development database...$(NC)"
	docker exec -it ${COMPOSE_PROJECT_NAME}-db psql -U ${POSTGRES_USER} -d ${POSTGRES_DB}

db-backup: ## Backup development database
	@echo "$(YELLOW)Backing up development database...$(NC)"
	@mkdir -p backup
	docker exec ${COMPOSE_PROJECT_NAME}-db pg_dump -U ${POSTGRES_USER} -d ${POSTGRES_DB} > backup/backup_$(shell date +%Y%m%d_%H%M%S).sql
	@echo "$(GREEN)Database backup completed!$(NC)"

db-migrate: ## Run database migrations (placeholder)
	@echo "$(YELLOW)Running database migrations...$(NC)"
	@echo "$(BLUE)Note: Add Flyway or Liquibase commands here$(NC)"

# ==============================================
# Utility Commands
# ==============================================

logs: ## Show all container logs
	docker-compose logs -f

status: ## Show container status
	@echo "$(BLUE)Container Status:$(NC)"
	docker-compose ps

health: ## Check application health
	@echo "$(YELLOW)Checking application health...$(NC)"
	curl -f http://localhost:$(SERVER_PORT)/api/v1/health || echo "$(RED)Application not healthy$(NC)"

clean-all: ## Clean everything (containers, images, volumes)
	@echo "$(RED)WARNING: This will remove all containers, images, and volumes!$(NC)"
	@read -p "Are you sure? [y/N] " -n 1 -r; \
	echo; \
	if [[ $$REPLY =~ ^[Yy]$$ ]]; then \
		docker-compose down -v --remove-orphans; \
		docker system prune -a -f --volumes; \
		echo "$(GREEN)Everything cleaned!$(NC)"; \
	else \
		echo "$(YELLOW)Operation cancelled.$(NC)"; \
	fi

# ==============================================
# CI/CD Commands
# ==============================================

ci-test: ## Run CI tests
	./mvnw clean test jacoco:report

ci-build: ## Build for CI
	./mvnw clean package -DskipTests

ci-docker: ## Build Docker image for CI
	docker build -t $(APP_NAME):ci -f Containerfile .

# ==============================================
# Development Tools
# ==============================================

format: ## Format code (placeholder)
	@echo "$(YELLOW)Code formatting...$(NC)"
	@echo "$(BLUE)Note: Add code formatting commands here$(NC)"

lint: ## Lint code (placeholder)
	@echo "$(YELLOW)Code linting...$(NC)"
	@echo "$(BLUE)Note: Add linting commands here$(NC)"

# ==============================================
# Documentation
# ==============================================

docs: ## Generate documentation
	@echo "$(YELLOW)Generating documentation...$(NC)"
	@echo "$(BLUE)Note: Add documentation generation commands here$(NC)"

services-up: ## Start only background services (db, redis, etc.) with Docker
	@echo "$(YELLOW)Starting background services with Docker...$(NC)"
	docker-compose up -d db redis pgadmin

services-down: ## Stop background services with Docker
	@echo "$(YELLOW)Stopping background services...$(NC)"
	docker-compose down

services-down: ## Stop background services with Docker
	@echo "$(YELLOW)Stopping background services...$(NC)"
	docker-compose down

services-down: ## Stop background services with Docker
	@echo "$(YELLOW)Stopping background services...$(NC)"
	docker-compose down 