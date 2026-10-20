---
name: Ecological Utility Interface
colors:
  surface: '#f9f9ff'
  surface-dim: '#d3daea'
  surface-bright: '#f9f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f0f3ff'
  surface-container: '#e7eefe'
  surface-container-high: '#e2e8f8'
  surface-container-highest: '#dce2f3'
  on-surface: '#151c27'
  on-surface-variant: '#3e4a3d'
  inverse-surface: '#2a313d'
  inverse-on-surface: '#ebf1ff'
  outline: '#6e7b6c'
  outline-variant: '#bdcaba'
  surface-tint: '#006e2d'
  primary: '#006b2c'
  on-primary: '#ffffff'
  primary-container: '#00873a'
  on-primary-container: '#f7fff2'
  inverse-primary: '#62df7d'
  secondary: '#0058be'
  on-secondary: '#ffffff'
  secondary-container: '#2170e4'
  on-secondary-container: '#fefcff'
  tertiary: '#755800'
  on-tertiary: '#ffffff'
  tertiary-container: '#936f00'
  on-tertiary-container: '#fffbff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#7ffc97'
  primary-fixed-dim: '#62df7d'
  on-primary-fixed: '#002109'
  on-primary-fixed-variant: '#005320'
  secondary-fixed: '#d8e2ff'
  secondary-fixed-dim: '#adc6ff'
  on-secondary-fixed: '#001a42'
  on-secondary-fixed-variant: '#004395'
  tertiary-fixed: '#ffdf9a'
  tertiary-fixed-dim: '#f7be1d'
  on-tertiary-fixed: '#251a00'
  on-tertiary-fixed-variant: '#5a4300'
  background: '#f9f9ff'
  on-background: '#151c27'
  surface-variant: '#dce2f3'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 4px
  margin-mobile: 16px
  gutter-mobile: 12px
  touch-target: 48px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 24px
---

## Brand & Style
The design system is centered on high-utility waste management and precision GPS mapping. The brand personality is dependable, environmental, and hyper-functional. It aims to evoke a sense of civic responsibility and efficiency, transforming a mundane task into a streamlined digital experience.

The design style follows a **Modern Corporate** aesthetic with a heavy emphasis on **Functional Utility**. Because the app is intended for outdoor use—often in bright sunlight or by users wearing gloves—the interface prioritizes high-contrast ratios, generous white space to reduce cognitive load, and large, "chunky" interactive elements. The aesthetic is clean and systematic, using color not just for decoration, but as a critical data-encoding tool for waste categorization.

## Colors
The color palette is divided into functional brand colors and semantic waste categories. 

- **Primary Green (#16A34A)** represents the "Eco-friendly" brand identity and is used for primary actions, success states, and brand-level navigation.
- **Waste Categories**: These five colors are reserved strictly for waste classification (icons, cards, and map markers). 
- **Neutral Grays**: Used for backgrounds and borders to maintain a clean, laboratory-like precision.

The default mode is **Light**, optimized for high-glare outdoor environments. A high-contrast background (#F9FAFB) is paired with deep slate text (#111827) to ensure maximum legibility for field operations.

## Typography
This design system uses **Inter** for its neutral, systematic character and exceptional legibility at small sizes. 

- **Hierarchy**: Bold weight is used for all headlines to ensure clear section scanning.
- **Readability**: For mobile utility, the minimum body size is set to 16px to accommodate users in motion.
- **Labels**: All-caps is avoided for labels to maintain a friendly, approachable tone; instead, medium or semi-bold weights are used for emphasis.
- **Numerical Data**: When displaying GPS coordinates or bin capacities, use tabular figures to ensure numbers align vertically in data lists.

## Layout & Spacing
The layout follows a **Fluid Grid** model optimized for mobile handsets. A 4px baseline grid ensures consistent vertical rhythm.

- **Safe Margins**: A 16px horizontal margin is mandatory for all screens to prevent content from hitting the edge of the device or being obscured by protective rugged cases.
- **Touch Fidelity**: Every interactive element must maintain a minimum height/width of 48px to support reliable input during outdoor use.
- **Stacking**: Use 16px (stack-md) as the default vertical spacing between related components (e.g., a card and its label) and 24px (stack-lg) between distinct sections.

## Elevation & Depth
To maintain a modern, clean utility look, this design system uses **Tonal Layers** rather than heavy shadows.

- **Surface 0 (Background)**: #F9FAFB. Used for the main app canvas.
- **Surface 1 (Cards)**: White (#FFFFFF) with a subtle 1px border (#E5E7EB). This provides a crisp, "paper-like" feel that performs well in sunlight.
- **Elevation**: Only active interactive states (like a dragged item) or critical modals use a soft, low-opacity ambient shadow (0px 4px 12px rgba(0, 0, 0, 0.05)).
- **Depth**: Mapping layers use semi-transparent overlays (80% opacity) to ensure the underlying map context remains visible while menus are open.

## Shapes
The shape language is **Rounded**, strike a balance between professional precision and approachable design. 

- **Primary Elements**: Standard buttons and cards use 0.5rem (8px) corners.
- **Icon Enclosures**: Waste category icons should be housed in circular containers or 1rem (16px) rounded squares to differentiate them from standard UI controls.
- **Input Fields**: Use the standard 8px radius to match buttons, creating a cohesive "block-based" look for all form elements.

## Components

- **Buttons**: Primary buttons are high-contrast green with white text. They occupy the full width of the mobile screen when used for main actions (e.g., "Confirm Collection").
- **Waste Chips**: Compact pills used for filtering. Each chip uses its respective waste color as a background at 10% opacity, with the text/border at 100% opacity.
- **Mapping Cards**: Floating bottom sheets provide details on bin locations. These must have a clear "handle" indicator and use 1rem (16px) rounded top corners.
- **Input Fields**: Large, high-visibility text inputs with clear 1px borders. Focused states use a 2px Primary Green border.
- **Lists**: Collection schedules use a list format with 16px padding and clear dividers. Each list item includes a colored waste-category indicator (e.g., a 4px vertical bar on the left edge).
- **Map Markers**: Custom iconography for each waste type, using the semantic color palette. Markers are slightly oversized (32px minimum) to remain tappable on high-density displays.