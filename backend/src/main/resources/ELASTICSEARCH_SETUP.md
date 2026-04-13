# Elasticsearch Implementation for HMS

## Overview

This document covers the implementation of Elasticsearch in the Hospital Management System (HMS) with support for:

- **Fuzzy Search**: Typo-tolerant search with automatic matching
- **Phonetic Search**: Match similar-sounding names (e.g., "Jon" matches "John")
- **Multi-field Search**: Search across multiple fields simultaneously
- **Batch Reindexing**: Efficient bulk indexing from database to Elasticsearch
- **Admin Interface**: Frontend UI for managing search indices

## Architecture

### Backend Structure

```
com/hms/common/search/
├── config/
│   └── ElasticsearchConfig.java           # Configuration and client setup
├── document/
│   ├── PatientDocument.java               # Search document model
│   ├── DoctorDocument.java
│   ├── AppointmentDocument.java
│   └── PrescriptionDocument.java
├── repository/
│   ├── PatientSearchRepository.java       # Spring Data ES repositories
│   ├── DoctorSearchRepository.java
│   ├── AppointmentSearchRepository.java
│   └── PrescriptionSearchRepository.java
├── service/
│   ├── SearchService.java                 # Generic search interface
│   ├── ElasticsearchReindexService.java   # Reindex interface
│   ├── ReindexStatus.java                 # Status DTO
│   └── impl/
│       ├── PatientSearchServiceImpl.java
│       ├── DoctorSearchServiceImpl.java
│       ├── AppointmentSearchServiceImpl.java
│       ├── PrescriptionSearchServiceImpl.java
│       └── ElasticsearchReindexServiceImpl.java  # Main reindex logic
├── controller/
│   ├── SearchController.java              # User search endpoints
│   └── AdminSearchController.java         # Admin reindex endpoints
└── listener/
    └── ElasticsearchInitializeListener.java  # Startup listener
```

### Indexed Entities

1. **Patients**: Name, contact number, email, address, blood group, urgency level
2. **Doctors**: First name, last name, specialization, department, qualification, email, phone
3. **Appointments**: Patient name, doctor name, reason, department, status, notes
4. **Prescriptions**: Patient name, doctor name, diagnosis, symptoms, medicines

## Setup Instructions

### Prerequisites

- Docker (for running Elasticsearch)
- Java 17+
- Angular 17+
- Spring Boot 3.3.4

### 1. Start Elasticsearch

#### Using Docker (Recommended)

```bash
# Single-node development cluster
docker run -d \
  --name elasticsearch \
  -e discovery.type=single-node \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  -p 9200:9200 \
  -p 9300:9300 \
  docker.elastic.co/elasticsearch/elasticsearch:8.11.0
```

#### Using Docker Compose

```yaml
version: "3.8"

services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data

volumes:
  elasticsearch_data:
```

### 2. Configure Application Properties

Add to `application.properties` or `application-local.properties`:

```properties
# Elasticsearch Configuration
elasticsearch.enabled=true
elasticsearch.host=localhost
elasticsearch.port=9200
elasticsearch.username=
elasticsearch.password=
spring.elasticsearch.rest.uris=http://localhost:9200
```

For production with authentication:

```properties
elasticsearch.host=your-elasticsearch-host.com
elasticsearch.port=9200
elasticsearch.username=${ELASTICSEARCH_USERNAME}
elasticsearch.password=${ELASTICSEARCH_PASSWORD}
```

### 3. Build and Run Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The application will:

- Detect Elasticsearch availability on startup
- Initialize the configuration
- Be ready to accept search requests and reindex commands

### 4. Access Admin Interface

The admin interface is typically accessible at:

```
http://localhost:4200/admin/elasticsearch
```

Or via the sidebar/menu in your admin panel.

## API Endpoints

### Admin Endpoints (Requires ADMIN role)

All endpoints require authentication and ADMIN role.

```bash
# Base URL
/api/v1/admin/search

# Reindex all patients
POST /reindex/patients
Response: { success: boolean, message: string, data: number (count) }

# Reindex all doctors
POST /reindex/doctors

# Reindex all appointments
POST /reindex/appointments

# Reindex all prescriptions
POST /reindex/prescriptions

# Perform full reindex (all entities)
POST /reindex/all
Response: { success: boolean, message: string, data: ReindexStatus }

# Clear all indices (DANGEROUS)
DELETE /indices/clear-all

# Check Elasticsearch health
GET /health
Response: { success: boolean, message: string, data: boolean }
```

### User Search Endpoints

```bash
# Base URL
/api/v1/search

# Search patients (fuzzy search)
GET /patients?query=john&page=0&size=10
Required roles: ADMIN, DOCTOR, RECEPTIONIST, PHARMACIST, LABORATORY_STAFF

# Search doctors (fuzzy search)
GET /doctors?query=cardiology&page=0&size=10
Required roles: ADMIN, PATIENT, RECEPTIONIST

# Search appointments (fuzzy search)
GET /appointments?query=knee&page=0&size=10
Required roles: ADMIN, DOCTOR, RECEPTIONIST

# Search prescriptions (fuzzy search)
GET /prescriptions?query=diabetes&page=0&size=10
Required roles: ADMIN, DOCTOR, PHARMACIST

# Exact field search for patients
GET /patients/exact?field=contactNumber&value=9876543210&page=0&size=10

# Exact field search for doctors
GET /doctors/exact?field=department&value=CARDIOLOGY&page=0&size=10
```

## Search Capabilities

### 1. Fuzzy Search

Fuzzy search automatically corrects typos and variations:

```
Query: "Jon Smith" → Matches "John Smith", "Jean Smith", etc.
Query: "Dr. Patel" → Matches "Doctor Patel", "Dr Patel", etc.
Query: "9876543210" → Matches contact numbers with small variations
```

### 2. Phonetic Search

Phonetic analysis matches similar-sounding names:

```
Query: "Smith" → Matches "Smythe", "Smyth"
Query: "John" → Matches "Jean", "Jon"
Query: "Garcia" → Matches "Garcea"
```

### 3. Multi-field Search

Queries search across relevant fields automatically:

**Patients**: Name, contact number, email, address
**Doctors**: First name, last name, specialization, department, email, phone
**Appointments**: Patient name, doctor name, reason, department, notes
**Prescriptions**: Patient name, doctor name, diagnosis, medicines, symptoms

### 4. Field Boosting

Certain fields are weighted more heavily for relevance:

- Patient name: 2x (higher priority)
- Doctor full name: 2x
- Contact/email: normal priority
- Other fields: normal priority

## Reindexing Strategy

### Batch Processing

Reindexing uses batch processing to efficiently handle large datasets:

- **Batch Size**: 500 records per batch
- **Memory Efficient**: Processes in transactions
- **Transactional**: Read-only transactions protect data consistency
- **Progress Logging**: Detailed logging of each batch

### Reindex Status

The reindex operation returns status information:

```json
{
  "patientCount": 1500,
  "doctorCount": 250,
  "appointmentCount": 5000,
  "prescriptionCount": 3500,
  "totalCount": 10250,
  "startTime": "2024-04-13T10:00:00",
  "endTime": "2024-04-13T10:05:30",
  "status": "SUCCESS",
  "errorMessage": null
}
```

## Frontend Admin Component

### Features

1. **Health Status**
   - Real-time Elasticsearch connectivity check
   - Refresh button to update status

2. **Reindex Operations**
   - Select specific entity types or all
   - Progress indication during reindexing
   - Detailed results with counts

3. **Index Management**
   - Clear all indices (with double confirmation)
   - Confirmation dialogs for dangerous operations

4. **Error Handling**
   - Detailed error messages
   - User-friendly alerts
   - Graceful degradation when Elasticsearch unavailable

### Component Usage

```typescript
import { ElasticsearchAdminComponent } from "./components/elasticsearch-admin/elasticsearch-admin.component";

// Add to your admin routing module
const routes: Routes = [
  {
    path: "elasticsearch",
    component: ElasticsearchAdminComponent,
    canActivate: [RoleGuard],
    data: { roles: [Role.ADMIN] },
  },
];
```

## Production Deployment

### Security Considerations

1. **Enable Security**

   ```yaml
   xpack.security.enabled: true
   ```

2. **Use Authentication**
   - Configure username and password
   - Store credentials in environment variables

3. **Network Isolation**
   - Run Elasticsearch in a private network
   - Use VPN or firewall rules for access

4. **Backup & Recovery**
   - Regular snapshots
   - Off-site backup storage
   - Document recovery procedures

### Performance Tuning

1. **Index Settings**

   ```json
   {
     "settings": {
       "number_of_shards": 3,
       "number_of_replicas": 1
     }
   }
   ```

2. **Memory Configuration**

   ```bash
   ES_JAVA_OPTS="-Xms2g -Xmx2g"
   ```

3. **Query Performance**
   - Use exact search for known fields
   - Adjust fuzziness based on requirements
   - Monitor slow query logs

### Monitoring

Monitor key metrics:

- Indexing rate
- Query latency
- Heap usage
- Disk I/O

Use Elasticsearch built-in monitoring or integrate with:

- Kibana
- Datadog
- New Relic
- Prometheus

## Troubleshooting

### Elasticsearch Not Available

```
Error: Elasticsearch is not available
Solution:
1. Check if Elasticsearch is running: curl http://localhost:9200
2. Verify network connectivity
3. Check application logs for connection errors
```

### Reindex Fails

```
Error: Connection timeout during reindex
Solution:
1. Increase Elasticsearch memory: ES_JAVA_OPTS="-Xms1g -Xmx1g"
2. Reduce batch size in code if needed
3. Check Elasticsearch logs: docker logs elasticsearch
```

### Slow Search Queries

```
Solution:
1. Check index statistics: GET /patients/_stats
2. Optimize query patterns
3. Add more replicas for better throughput
4. Increase Elasticsearch heap memory
```

### Disk Space Issues

```
Solution:
1. Clean up old indices: DELETE /patients_v1
2. Increase disk space
3. Enable index lifecycle management (ILM)
4. Archive old data
```

## Index Mapping Reference

Each index uses the following mapping strategy:

- **Keyword fields**: For exact matching (status, department, etc.)
- **Text fields**: For full-text search with analysis
- **Phonetic fields**: Separate fields for phonetic analysis
- **Searchable fields**: Dedicated fields for enhanced search experience

### Example Index Mapping

```json
{
  "mappings": {
    "properties": {
      "name": { "type": "keyword" },
      "nameSearchable": { "type": "text", "analyzer": "standard" },
      "namePhonetic": { "type": "text", "analyzer": "phonetic_analyzer" },
      "email": { "type": "keyword" },
      "emailSearchable": { "type": "text", "analyzer": "standard" },
      "createdAt": { "type": "date" },
      "deleted": { "type": "boolean" }
    }
  }
}
```

## Best Practices

### Development

- Use local Elasticsearch instance
- Enable detailed logging
- Test reindex with subset of data first

### Testing

- Test fuzzy search with typos
- Test phonetic matching
- Verify pagination
- Check performance with large datasets

### Operations

- Regular backups
- Monitor cluster health
- Set up alerts for failures
- Document recovery procedures
- Plan for scaling

## Integration with Existing Services

The Elasticsearch implementation is independent and doesn't break existing functionality:

- Patient, Doctor, Appointment, Prescription services continue working normally
- Search is additive - not required for CRUD operations
- Can be disabled by setting `elasticsearch.enabled=false`

## Future Enhancements

Potential improvements:

- Autocomplete with completion suggester
- Aggregations for analytics dashboards
- Search analytics and popular queries
- Synonym support for medical terms
- Document filtering with complex boolean queries
- Real-time indexing with event listeners
- Search performance optimization with caching

## Support and Documentation

- Elasticsearch Official Docs: https://www.elastic.co/guide/en/elasticsearch/reference/current/
- Spring Data Elasticsearch: https://spring.io/projects/spring-data-elasticsearch
- Phonetic Analysis: https://www.elastic.co/guide/en/elasticsearch/plugins/current/analysis-phonetic.html

## License

This Elasticsearch implementation follows the same license as the HMS project.

---

**Last Updated**: April 2024
**Version**: 1.0
