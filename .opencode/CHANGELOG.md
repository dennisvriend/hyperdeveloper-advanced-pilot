# Changelog

| Date | Feature | Summary |
| ---- | ------- | ------- |
| 20261005 | pets page | Added /pets list and detail pages with search, using existing backend endpoints |
| 20261005 | theme support | Light / Dark / System / Disco theme picker in header with localStorage persistence, FOUC prevention, and reduced-motion-safe disco animation |
| 20261005 | pet visit history | Global /visits page with full context (pet name + type badge, owner, vet), live search on pet/owner/description/date, newest-first ordering, click-through to pet detail, and visit count on pet detail page |
| 20261005 | vet attribution | Visit↔Vet relationship end-to-end: Liquibase migration (nullable vet_id FK, ON DELETE SET NULL), vetId on create/update visit APIs with 404 on unknown vet, vet summary in visit responses, vet picker in the visit form, vet names in both history views; fixed openapi-sync.sh health check for the /api context path |