# Media gallery

Visual placeholders aligned with [`source/ProjectOverview.md`](source/ProjectOverview.md) (`mediaGallery` and `mediaItems`). Replace every `PLACEHOLDER` URL with real **S3** or **CloudFront** assets after you capture screenshots from AWS or Compose.

## Gallery section (hero strip)

**Title:** Media gallery (placeholder assets)  
**Description:** Replace URLs with S3 or CloudFront paths after you capture screenshots and diagrams from the AWS environment.

| # | Type | Title | Category | URL | Thumbnail |
|---|------|-------|----------|-----|------------|
| 1 | Image | Spring Modulith module graph | architecture | [placeholder](https://cdn.PLACEHOLDER.example/bank-api/gallery-01-modulith.png) | [thumb](https://cdn.PLACEHOLDER.example/bank-api/gallery-01-modulith-thumb.png) |
| 2 | Image | Swagger UI on ALB | screenshot | [placeholder](https://cdn.PLACEHOLDER.example/bank-api/gallery-02-swagger.png) | [thumb](https://cdn.PLACEHOLDER.example/bank-api/gallery-02-swagger-thumb.png) |
| 3 | Image | Grafana JVM dashboard | screenshot | [placeholder](https://cdn.PLACEHOLDER.example/bank-api/gallery-03-grafana.png) | [thumb](https://cdn.PLACEHOLDER.example/bank-api/gallery-03-grafana-thumb.png) |
| 4 | Video | Architecture walkthrough | demo | [placeholder](https://cdn.PLACEHOLDER.example/bank-api/walkthrough.mp4) | [thumb](https://cdn.PLACEHOLDER.example/bank-api/walkthrough-thumb.png) |

**Captions (from source)**

1. **Modulith:** Placeholder for Modulith documentation or build-time module graph screenshot.  
2. **Swagger:** Placeholder for Swagger UI against staging/production ALB (consider IP restriction).  
3. **Grafana:** Compose today; Amazon Managed Grafana or self-hosted on AWS later.  
4. **Walkthrough:** Short Loom or S3-hosted MP4 for request and event flows.

## Additional media items

| Type | Title | Category | URL | Notes |
|------|-------|----------|-----|--------|
| Image | AWS reference architecture | architecture | [placeholder](https://cdn.PLACEHOLDER.example/bank-api/media-arch.png) | ALB, ECS, RDS, ElastiCache, MSK — export from draw.io or Lucidchart |
| Image | Transfer sequence | diagram | [placeholder](https://cdn.PLACEHOLDER.example/bank-api/media-seq-transfer.png) | Client → ALB → `TransferController` → domain → events → ledger listener |
| Image | Persistence overview | diagram | [placeholder](https://cdn.PLACEHOLDER.example/bank-api/media-er.png) | ER placeholder — **Flyway** migrations remain source of truth |

## Accessibility

- Provide meaningful **alt** text for each final image (see YAML `alt` fields in source).  
- For video, add a text transcript or chapter markers for long walkthroughs.

## Related

- [Project overview](ProjectOverview.md) — narrative context  
- [Project links](ProjectLinks.md) — GitHub and future CDN base URLs  
