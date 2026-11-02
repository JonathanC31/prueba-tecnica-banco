# Guía de Despliegue en AWS Elastic Beanstalk (Región `us-east-1` N. Virginia)

Esta guía resume el proceso exacto para desplegar la plataforma financiera (**PostgreSQL 16 + API Spring Boot + Frontend Angular/Nginx**) sobre una única instancia **EC2 `t3.micro` (1 GB RAM)** usando **AWS Elastic Beanstalk (Docker)** y **GitHub Actions**.

---

## 1. Arquitectura de Despliegue y Presupuesto de Memoria

En lugar de compilar en la instancia `t3.micro` (lo que agotaría su 1 GB de RAM), el pipeline de **GitHub Actions** compila el código, ejecuta las pruebas JUnit/Mockito, construye imágenes Docker multi-stage ultraligeras (`jlink` + `alpine`) y las publica en **GitHub Container Registry (`ghcr.io`)**. Elastic Beanstalk solo descarga las imágenes listas y levanta el `docker-compose.yml`.

| Componente | Imagen Base | Límite RAM (`mem_limit`) | Uso típico en reposo |
|---|---|---|---|
| **Base de datos (`db`)** | `postgres:16-alpine` | `200 MB` | ~60–110 MB |
| **Backend (`api`)** | `alpine:3.21` + `jlink` JRE 21 | `450 MB` (`MaxRAMPercentage=65`) | ~150–230 MB |
| **Frontend (`web`)** | `nginxinc/nginx-unprivileged:alpine` | `64 MB` | ~10–15 MB |
| **SO + Docker + Swap** | Amazon Linux 2023 + 1 GB Swap (`.ebextensions`) | Resto + 1 GB Swap | ~200 MB |

---

## 2. Paso a Paso en la Consola de AWS (`us-east-2`)

### Paso 1: Crear alerta de presupuesto (AWS Budgets)
1. Ir a **Billing and Cost Management → Budgets → Create budget**.
2. Seleccionar **Zero spend budget** (o *Monthly cost budget* con límite de `$1.00 USD`) y registrar tu correo electrónico.

### Paso 2: Crear el rol de perfil de instancia EC2 (`IAM`)
1. Ir a **IAM → Roles → Create role**.
2. Seleccionar **AWS service** y caso de uso **EC2**.
3. Adjuntar las políticas administradas:
   - `AWSElasticBeanstalkWebTier`
   - `AWSElasticBeanstalkWorkerTier`
   - `AWSElasticBeanstalkMulticontainerDocker`
4. Nombrar el rol: `aws-elasticbeanstalk-ec2-role`.

### Paso 3: Crear la aplicación y entorno en Elastic Beanstalk
1. Ir a **Elastic Beanstalk → Create application**.
2. **Environment tier**: *Web server environment*.
3. **Application name**: `prueba-tecnica-banco`
4. **Environment name**: `banco-prod`
5. **Platform**: *Docker* (`Docker running on 64bit Amazon Linux 2023`).
6. **Application code**: *Sample application*.
7. **Presets**: **Single instance (free tier eligible)**.
8. **Service access**:
   - *Service role*: crear/usar `aws-elasticbeanstalk-service-role`.
   - *EC2 instance profile*: seleccionar `aws-elasticbeanstalk-ec2-role`.
9. **Networking**: seleccionar la VPC por defecto, activar **Public IP address** (`Activated`) y marcar al menos una subred pública.
10. **Instance types**: dejar únicamente **`t3.micro`** (o `t2.micro`).
11. **Environment properties (Variables de entorno)**:
    - `DB_PASSWORD`: contraseña segura para PostgreSQL en producción.
    - `POSTGRES_DB`: `banco`
    - `POSTGRES_USER`: `prueba`
    - `SPRING_JPA_HIBERNATE_DDL_AUTO`: `update`

### Paso 4: Visibilidad pública de paquetes en GHCR
Una vez que el workflow de GitHub Actions publique las imágenes por primera vez en `ghcr.io/jonathanc31/prueba-tecnica-banco` y `ghcr.io/jonathanc31/prueba-tecnica-front`:
1. Ir a tu perfil de GitHub → pestaña **Packages**.
2. Entrar al paquete → **Package settings** → **Change visibility** → **Public** (para que la instancia EC2 pueda hacer `docker pull` sin requerir tokens en AWS).

### Paso 5: Configurar secretos en GitHub
En la terminal local con GitHub CLI (`gh`):
```bash
gh secret set AWS_ACCESS_KEY_ID -R JonathanC31/prueba-tecnica-banco
gh secret set AWS_SECRET_ACCESS_KEY -R JonathanC31/prueba-tecnica-banco
```

### Paso 6: Apagar recursos al finalizar la evaluación
Para evitar consumo de créditos tras la entrevista:
- En **Elastic Beanstalk → Environments → `banco-prod` → Actions → Terminate environment**.
