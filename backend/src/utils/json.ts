export function parseStringArray(value: string | null | undefined): string[] {
  if (!value) return [];
  try {
    const parsed = JSON.parse(value);
    if (Array.isArray(parsed)) {
      return parsed.filter((item) => typeof item === 'string');
    }
  } catch {
    if (value.includes(';;;')) return value.split(';;;').filter(Boolean);
  }
  return [];
}

export function stringifyStringArray(value: string[] | undefined | null): string {
  return JSON.stringify(value ?? []);
}

export function ageFromDob(dateOfBirth: Date): number {
  const now = new Date();
  let age = now.getFullYear() - dateOfBirth.getFullYear();
  const monthDiff = now.getMonth() - dateOfBirth.getMonth();
  if (monthDiff < 0 || (monthDiff === 0 && now.getDate() < dateOfBirth.getDate())) {
    age -= 1;
  }
  return age;
}

export function dobFromAge(age: number): Date {
  const date = new Date();
  date.setFullYear(date.getFullYear() - age);
  date.setMonth(0, 1);
  return date;
}
