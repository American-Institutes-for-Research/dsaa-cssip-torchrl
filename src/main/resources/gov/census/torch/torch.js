importPackage(Packages.gov.census.torch);
importPackage(Packages.gov.census.torch.comparators);
importPackage(Packages.gov.census.torch.counter);
importPackage(Packages.gov.census.torch.model);
importPackage(Packages.gov.census.torch.io);

EXACT = StandardComparators.EXACT;
PRORATED = StandardComparators.PRORATED;
YEAR = StandardComparators.YEAR;
STRING = StandardComparators.STRING;

function isDefined(x) {
    return (typeof x != 'undefined');
}

Script = {
    newFixedWidthFileSchema: function(obj) {
        var b = new FixedWidthFileSchema.Builder();

        var columns = obj['columns'];
        if (!isDefined(columns)) {
            throw "file schema must include 'columns'";
        }

        for (var i = 0; i < columns.length; i++) {
            var f = columns[i];
            b.column(f[0], f[1], f[2]);
        }

        var blockingFields = obj['blockingFields'];
        if (isDefined(blockingFields)) {
            for (var i = 0; i < blockingFields.length; i++) {
                b.blockingField(blockingFields[i]);
            }
        }

        var idFields = obj['idFields'];
        if (isDefined(idFields)) {
            for (var i = 0; i < idFields.length; i++) {
                b.idField(idFields[i]);
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
