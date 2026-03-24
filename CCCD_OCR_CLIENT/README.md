# CCCD OCR Client

Frontend client for OCR workflow.

## Project structure (best-practice baseline)

```text
src/
	app/                    # App shell, providers, app-level config
	assets/                 # Static assets (images, icons, fonts)
	components/
		common/               # Shared reusable UI components
	features/
		ocr/                  # Feature module (components/hooks/services)
	pages/                  # Route-level pages/screens
	shared/
		constants/            # App constants
		utils/                # Shared utility functions
	styles/                 # Global styles, theme tokens
	main.jsx                # Entry point
```

## Path alias

- Use `@/` mapped to `src/`.
- Example: `import App from '@/app/App'`

## Scripts

- `npm run dev` – start development server
- `npm run build` – create production build
- `npm run preview` – preview production build
- `npm run lint` – run ESLint

## Naming convention

- Components: `PascalCase` (`SectionTitle.jsx`)
- Hooks: `camelCase` prefixed with `use` (`useOcrUpload.js`)
- Constants: `UPPER_SNAKE_CASE` inside `*.constants.js`
- Feature folders: lowercase (`features/ocr`)

## Next recommended steps

1. Add router (`react-router-dom`) if multi-page.
2. Add API layer (Axios + interceptors) in `features/*/services`.
3. Add env schema validation for `.env`.
4. Add tests (`vitest`, `@testing-library/react`).
