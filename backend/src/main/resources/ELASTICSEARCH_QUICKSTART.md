# Elasticsearch Quick Start Guide for HMS

## 5-Minute Setup

### Step 1: Start Elasticsearch (Docker)

```bash
docker run -d \
  --name elasticsearch \
  -e discovery.type=single-node \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  -p 9200:9200 \
  docker.elastic.co/elasticsearch/elasticsearch:8.11.0
```

Verify it's running:

```bash
curl http://localhost:9200
```

### Step 2: Build and Start Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Step 3: Access Admin Dashboard

1. Login to HMS frontend at `http://localhost:4200`
2. Navigate to Admin → Elasticsearch Management
3. Click "Refresh" to check Elasticsearch health
4. Select "All Entities" from dropdown
5. Click "Start Reindex"

### Step 4: Use Search

The search is now available at:

- `/api/v1/search/patients?query=john`
- `/api/v1/search/doctors?query=cardiology`
- `/api/v1/search/appointments?query=knee`
- `/api/v1/search/prescriptions?query=diabetes`

## Key Configuration

**application.properties:**

```properties
elasticsearch.enabled=true
elasticsearch.host=localhost
elasticsearch.port=9200
```

**For Production:**

```properties
elasticsearch.host=${ELASTICSEARCH_HOST}
elasticsearch.port=${ELASTICSEARCH_PORT}
elasticsearch.username=${ELASTICSEARCH_USERNAME}
elasticsearch.password=${ELASTICSEARCH_PASSWORD}
```

## Common Tasks

### Reindex All Data

```
POST http://localhost:8080/api/v1/admin/search/reindex/all
```

### Reindex Specific Entity

```
POST http://localhost:8080/api/v1/admin/search/reindex/patients
POST http://localhost:8080/api/v1/admin/search/reindex/doctors
POST http://localhost:8080/api/v1/admin/search/reindex/appointments
POST http://localhost:8080/api/v1/admin/search/reindex/prescriptions
```

### Check Status

```
GET http://localhost:8080/api/v1/admin/search/health
```

### Search Patients

```
GET http://localhost:8080/api/v1/search/patients?query=john&page=0&size=10
```

## Test Fuzzy Search

Try these searches to verify fuzzy matching works:

- Search "jon" → matches "john"
- Search "smyth" → matches "smith"
- Search "9876543211" (off by one digit) → matches "9876543210"

## Troubleshooting

### "Elasticsearch is not available"

- Check Docker: `docker ps | grep elasticsearch`
- Restart: `docker restart elasticsearch`
- Check logs: `docker logs elasticsearch`

### Reindex Takes Too Long

- Reduce batch size in `ElasticsearchReindexServiceImpl.java`
- Increase Elasticsearch memory: `ES_JAVA_OPTS="-Xms1g -Xmx1g"`

### Disk Space Issues

- Clear indices: `DELETE http://localhost:9200/patients`
- Clean up Docker volumes: `docker volume prune`

## Next Steps

1. Review [ELASTICSEARCH_SETUP.md](./ELASTICSEARCH_SETUP.md) for detailed configuration
2. Setup Kibana for monitoring: `http://localhost:5601`
3. Configure backups for production
4. Performance tune based on your data volume

## Support

For detailed documentation, see: **backend/src/main/resources/ELASTICSEARCH_SETUP.md**
