# Haven Android - Agent Guidelines

## Constraints

- **Max File Size**: 420 lines per Kotlin source file

## Coding Guidelines

- **Compile-Time Safety**: Code must be compile-time safe. Do not use reflection or runtime method lookup. All method calls should be validated at compile time to catch errors early.
- **No Reflection**: Avoid using `java.lang.reflect` APIs. If a method doesn't exist at compile time, it should fail at build time, not runtime.

## UI Guidelines

- **Font**: Use Inter font family for all text. The typography is configured in `ui/theme/Type.kt` with InterFontFamily applied to all text styles.
