---
name: vethub:backend
description: use when to work with the backend
---

# Start the backend

Start the server as a detached background process so the shell returns immediately:

```shell { workdir="server" }
nohup ./start.sh > /tmp/vethub-backend.log 2>&1 &
echo "Backend starting... PID: $!"
```

Then poll until the server is ready (returns HTTP 200):

```shell
for i in $(seq 1 30); do
  if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/v1/owners | grep -q "200"; then
    echo "Backend is ready"
    break
  fi
  echo "Waiting for backend... ($i/30)"
  sleep 2
done
```

> The first run takes 30-60 seconds while Gradle downloads dependencies. Subsequent starts take ~3 seconds.

# Verify it is running

Confirm each endpoint returns a successful response:

| Endpoint   | URL                                                          | Expected Response     |
|------------|--------------------------------------------------------------|-----------------------|
| Swagger UI | http://localhost:8080/api/v1/public/docs/swagger-ui/index.html | API documentation page |
| Owners API | http://localhost:8080/api/v1/owners                          | JSON array of owners  |
| Vets API   | http://localhost:8080/api/v1/vets                            | JSON array of vets    |
| Pets API   | http://localhost:8080/api/v1/pets                            | JSON array of pets    |

> Note: The H2 database is seeded automatically on startup with sample data (owners, pets, vets, specialties, visits).

# Database

The backend uses an **H2 in-memory database** managed by **Liquibase**.

## Connection Details

| Property   | Value                                                              |
|------------|--------------------------------------------------------------------|
| URL        | `jdbc:h2:mem:demodb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`    |
| Username   | `sa`                                                               |
| Password   | *(empty)*                                                          |
| Driver     | `org.h2.Driver`                                                    |
| H2 Version | `2.3.232`                                                          |

> All values are overridable via environment variables: `DATASOURCE_URL`, `DATASOURCE_USERNAME`, `DATASOURCE_PASSWORD`.

## Schema

Managed by Liquibase. Changelog entry point: `server/src/main/resources/db/changelog/db.changelog-master.yaml`

- `prd` context: creates schema only
- `tst` context: creates schema + seeds test data
- Dev profile runs with `drop-first: true` and context `tst`

## Tables

| Table              | Entity     | Description                     |
|--------------------|------------|---------------------------------|
| `owners`           | `Owner`    | Pet owners (id, name, address)  |
| `pets`             | `Pet`      | Pets (id, name, birth_date, FK to owner + type) |
| `pet_types`        | `PetType`  | Pet categories (Cat, Dog, etc.) |
| `vets`             | `Vet`      | Veterinarians (id, name)        |
| `specialties`      | `Specialty`| Vet specialties                 |
| `vet_specialties`  | *(join)*   | Vet <-> Specialty junction      |
| `visits`           | `Visit`    | Pet visits (id, date, description) |

### Entity Relationships

```
Owner  1──*  Pet  *──1  PetType
              |
              1──*  Visit

Vet  *──*  Specialty  (via vet_specialties)
```

## Seed Data (context: `tst`)

| Table            | Count | Details                                                        |
|------------------|-------|----------------------------------------------------------------|
| `pet_types`      | 6     | Cat, Dog, Lizard, Snake, Bird, Hamster                        |
| `specialties`    | 3     | Radiology, Surgery, Dentistry                                  |
| `vets`           | 6     | James Carter, Helen Leary, Linda Douglas, Rafael Ortega, Henry Stevens, Sharon Jenkins |
| `vet_specialties`| 5     | Vet-specialty associations                                     |
| `owners`         | 10    | George Franklin, Betty Davis, Eduardo Rodriquez, etc.         |
| `pets`           | 13    | Leo, Basil, Rosy, Jewel, Iggy, George, Samantha, Max, etc.    |
| `visits`         | 4     | Rabies shots, Spayed, Neutered                                 |

## Query the Database

```shell
curl -s http://localhost:8080/api/v1/owners | python3 -m json.tool
curl -s http://localhost:8080/api/v1/vets | python3 -m json.tool
curl -s http://localhost:8080/api/v1/pets | python3 -m json.tool
```

# Stop the backend

```shell
kill $(lsof -t -i :8080) 2>/dev/null && echo "Backend stopped" || echo "No process on port 8080"
```
