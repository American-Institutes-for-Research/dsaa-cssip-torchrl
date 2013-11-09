importPackage(Packages.gov.census.torch);
importPackage(Packages.gov.census.torch.comparators);
importPackage(Packages.gov.census.torch.counter);
importPackage(Packages.gov.census.torch.io);

EXACT = StandardComparators.EXACT;
PRORATED = StandardComparators.PRORATED;
YEAR = StandardComparators.YEAR;
STRING = StandardComparators.STRING;

function isDefined(x) {
    return (typeof x != 'undefined');
}

Script = {
    newFileSchema: function(obj) {
        var b = new FixedWidthFileSchema.Builder();

        var fields = obj['fields'];
        if (!isDefined(fields)) {
            throw "file_schema must include 'fields'";
        }

        for (var i = 0; i < fields.length; i++) {
            var f = fields[i];
            b.field(f[0], f[1], f[2]);
        }

        var blockingFields = obj['blockingFields'];
        if (isDefined(blockingFields)) {
            for (var i = 0; i < blockingFields.length; i++) {
                var f = blockingFields[i];
                var k = (typeof f[0] == 'string') ? 1 : 0;
                b.blockingField(f[0 + k], f[1 + k]);
            }
        }

        var idFields = obj['idFields'];
        if (isDefined(idFields)) {
            for (var i = 0; i < idFields.length; i++) {
                var f = idFields[i];
                var k = (typeof f[0] == 'string') ? 1 : 0;
                b.idField(f[0 + k], f[1 + k]);
            }
        }

        return b.build();
    },

    newRecordComparator: function(schema1, schema2, ary) {
        var b = new RecordComparator.Builder(schema1, schema2);

        for (var i = 0; i < ary.length; i++) {
            c = ary[i];
            b.comparator(c[0], c[1]);
        }

        return b.build();
    }
}

$ = Script;
