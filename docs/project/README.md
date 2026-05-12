# Bank API — project documentation

Human-readable documentation for the Bank API modular monolith. These pages are generated from the same facts as the machine-readable YAML in [`source/`](source/) (see [`source/TypescriptSchema.md`](source/TypescriptSchema.md) for the schema contract).

## Navigation

| Document | What you will find |
|----------|-------------------|
| [ProjectMetadata.md](ProjectMetadata.md) | Identity: name, version, status, tech stack, repository, portfolio notes |
| [ProjectOverview.md](ProjectOverview.md) | Problem statement, solution pillars, snapshot metrics, risks |
| [ProjectLinks.md](ProjectLinks.md) | GitHub, demo, docs tree, Docker Hub (if any) |
| [ProjectMetric.md](ProjectMetric.md) | Product and infrastructure metrics in one place |
| [ProjectFeatures.md](ProjectFeatures.md) | Feature catalog with categories, status, highlights |
| [ProjectArchitectureModel.md](ProjectArchitectureModel.md) | Layers, patterns, AWS-shaped diagram, data flows, ADRs |
| [InfrastructureModel.md](InfrastructureModel.md) | Local, Docker Compose, and AWS deployment views, Dockerfile reference |
| [APISchema.md](APISchema.md) | REST endpoints, auth, rate limits, response patterns |
| [ProjectCodeShowCase.md](ProjectCodeShowCase.md) | Short code excerpts with file paths into the repo |
| [MediaGallerySection.md](MediaGallerySection.md) | Placeholder gallery for screenshots, diagrams, walkthrough video |

## Source data

Structured YAML (for tools, portfolios, or static site generators) lives under [`source/`](source/). When the two drift, treat **`source/`** as canonical for field names and **`*.md` here** as the readable companion.

## Conventions in this folder

- Paths like `bank-boot/...` are relative to the **repository root**, not this folder.
- **AWS** sections describe the **target** production layout (ALB, ECS Fargate, RDS, ElastiCache, MSK). Local development may still use Docker Compose without TLS on port 80.
- **PLACEHOLDER** hostnames and `cdn.PLACEHOLDER.example` URLs are intentional until you wire real DNS and S3/CloudFront assets.
