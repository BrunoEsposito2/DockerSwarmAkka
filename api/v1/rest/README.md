# REST API Documentation v1

**Version:** 1.0.11  
**Standard:** OpenAPI 3.0.3

## Files

- [**Interactive UI**](./index.html) - Test the API in your browser
- [**OpenAPI YAML**](./openapi.yaml) - Human-readable specification
- [**OpenAPI JSON**](./openapi.json) - Machine-readable specification

## Usage

1. **Browser testing**: Open [index.html](./index.html)
2. **Import to tools**: Use the YAML or JSON files
3. **Generate clients**: Use OpenAPI Generator with the spec files

## Quick Examples

### Get System Status
```bash
curl http://localhost:4000/status
```

### Switch Camera
```bash
curl -X POST http://localhost:4000/camera/switch \
  -H "Content-Type: application/json" \
  -d '{"cameraId": "camera1"}'
```

### Set Detection Window
```bash
curl -X POST http://localhost:4000/window \
  -H "Content-Type: application/json" \
  -d '{"x": 100, "y": 50, "width": 400, "height": 300}'
```

## Servers

- Development: http://localhost:4000
