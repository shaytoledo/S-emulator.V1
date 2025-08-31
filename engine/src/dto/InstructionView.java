package dto;

public record InstructionView(
        int number,
        String type,
        String  label,
        String command,
        int cycles) {


    @Override
    public String toString() {
        String typeFlag =type.toUpperCase();
        String safeLabel = (label == null) ? "" : label;
        String paddedLabel = String.format("%-3s", safeLabel);
        String cmd = (command == null || command.isBlank()) ? "?" : command.trim();
        return "# " + number + " (" + typeFlag + ") "+ "[" + paddedLabel + "]" + " " + cmd + " (" + cycles + ")";
    }
}
