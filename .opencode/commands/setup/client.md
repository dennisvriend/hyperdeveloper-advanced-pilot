---
name: vethub:client
description: use when to work with the frontend
---

# Start the frontend

Start the client as a detached background process so the shell returns immediately:

```shell { workdir="client" }
nohup ./start.sh > /tmp/vethub-frontend.log 2>&1 &
echo "Frontend starting... PID: $!"
```

Then poll until the client is ready:

```shell
for i in $(seq 1 30); do
  if curl -s -o /dev/null -w "%{http_code}" http://localhost:5173 | grep -q "200"; then
    echo "Frontend is ready"
    break
  fi
  echo "Waiting for frontend... ($i/30)"
  sleep 2
done
```

> The first run takes longer while bun downloads dependencies. Subsequent starts are faster.

# Verify it is running

| Endpoint | URL                | Expected Response |
|----------|--------------------|-------------------|
| Client   | http://localhost:5173 | Pet Clinic UI     |

# Stop the frontend

```shell
kill $(lsof -t -i :5173) 2>/dev/null && echo "Frontend stopped" || echo "No process on port 5173"
```
