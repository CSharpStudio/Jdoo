# Jdoo
Java odoo

move python code to java

python model declare
```python
class Partner(models.Model):
    _description = 'Contact'
    _inherit = ['format.address.mixin', 'image.mixin']
    _name = "res.partner"
    _order = "display_name"
```

java model declare
```java
public class Partner extends Model {
    public Partner() {
        _description = "Contact";
        _inherits = new String[] { "format.address.mixin", "image.mixin" };
        _name = "res.partner";
        _order = "display_name";
    }
}
```

python fields declare
```python
class Partner(models.Model):
    name = fields.Char(index=True)
    display_name = fields.Char(compute='_compute_display_name', store=True, index=True)
    date = fields.Date(index=True)
    title = fields.Many2one('res.partner.title')
    parent_id = fields.Many2one('res.partner', string='Related Company', index=True)
    parent_name = fields.Char(related='parent_id.name', readonly=True, string='Parent name')
    child_ids = fields.One2many('res.partner', 'parent_id', string='Contact', domain=[('active', '=', True)])  # force "active_test" domain to bypass _search() override
    ref = fields.Char(string='Reference', index=True)
    lang = fields.Selection(_lang_get, string='Language', default=lambda self: self.env.lang,
                            help="All the emails and documents sent to this contact will be translated in this language.")
    active_lang_count = fields.Integer(compute='_compute_active_lang_count')
    tz = fields.Selection(_tz_get, string='Timezone', default=lambda self: self._context.get('tz'),
                          help="When printing documents and exporting/importing data, time values are computed according to this timezone.\n"
                               "If the timezone is not set, UTC (Coordinated Universal Time) is used.\n"
                               "Anywhere else, time values are computed according to the time offset of your web client.")
```

java fields declare
```java
public class Partner extends Model {
    static Field name = fields.Char().index(true);
    static Field display_name = fields.Char().compute("_compute_display_name").store(true).index(true);
    static Field date = fields.Date().index(true);
    static Field title = fields.Many2one("res.partner.title");
    static Field parent_id = fields.Many2one("res.partner").string("Related Company").index(true);
    static Field parent_name = fields.Char().related("parent_id.name").readonly(true).string("Parent name");
    static Field child_ids = fields.One2many("res.partner", "parent_id").string("Contact")
            .domain(d.on("active", "=", true));// # force "active_test" domain to bypass _search() override
    static Field ref = fields.Char().string("Reference").index(true);
    static Field lang = fields.Selection(self -> _lang_get(self)).string("Language").default_(self -> self.env().lang())
            .help("All the emails and documents sent to this contact will be translated in this language.");
    static Field active_lang_count = fields.Integer().compute("_compute_active_lang_count");
    static Field tz = fields.Selection(self -> _tz_get(self)).string("Timezone")
            .default_(self -> self.context().get("tz"))
            .help("When printing documents and exporting/importing data, time values are computed according to this timezone.\n"
                    + "If the timezone is not set, UTC (Coordinated Universal Time) is used.\n"
                    + "Anywhere else, time values are computed according to the time offset of your web client.");
}
```

python methods declare
```python
class Partner(models.Model):
    @api.depends('is_company', 'name', 'parent_id.name', 'type', 'company_name')
    @api.depends_context('show_address', 'show_address_only', 'show_email', 'html_format', 'show_vat')
    def _compute_display_name(self):
        diff = dict(show_address=None, show_address_only=None, show_email=None, html_format=None, show_vat=None)
        names = dict(self.with_context(**diff).name_get())
        for partner in self:
            partner.display_name = names.get(partner.id)
```

java methods declare
```java
public class Partner extends Model {
    @api.depends({ "is_company", "name", "parent_id.name", "type", "company_name" })
    @api.depends_context({ "show_address", "show_address_only", "show_email", "html_format", "show_vat" })
    public void _compute_display_name(Self self) {
        self.with_context(new Kvalues().set("show_address",null).set("show_address_only",null).set("show_email",null).set("html_format",null).set("show_vat",null));
        Kvalues names = self.call(Kvalues.class, "name_get");
        for (Self partner : self)
            partner.set(display_name, names.get(partner.id()));
    }
}
```