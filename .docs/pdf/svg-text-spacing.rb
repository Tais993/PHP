require 'asciidoctor-pdf'

# prawn-svg 0.34.2 can emit SVG character spacing in scientific notation,
# which PDF readers cannot parse. Format those Tc operands as decimals.
module SvgTextSpacing
  def add_content(content)
    super(content.sub(/\A(-?\d+(?:\.\d+)?e[+-]\d+) Tc\z/i) do
      format('%.8f Tc', Regexp.last_match(1).to_f)
    end)
  end
end

Prawn::Document.prepend(SvgTextSpacing)
