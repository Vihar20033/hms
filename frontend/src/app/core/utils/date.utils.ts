export class DateUtils {
  
  /**
   * Formats Date to YYYY-MM-DD
   */
  static formatDate(value: Date | null): string | null {
    if (!value) return null;
    const year = value.getFullYear();
    const month = String(value.getMonth() + 1).padStart(2, '0');
    const day = String(value.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  /**
   * Formats Date to ISO format used by LocalDateTime in backend (YYYY-MM-DDTHH:mm:ss)
   */
  static formatLocalDateTime(value: Date): string {
    const year = value.getFullYear();
    const month = String(value.getMonth() + 1).padStart(2, '0');
    const day = String(value.getDate()).padStart(2, '0');
    const hours = String(value.getHours()).padStart(2, '0');
    const mins = String(value.getMinutes()).padStart(2, '0');
    const secs = String(value.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${mins}:${secs}`;
  }
}
