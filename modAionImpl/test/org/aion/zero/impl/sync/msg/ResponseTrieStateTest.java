package org.aion.zero.impl.sync.msg;

import static com.google.common.truth.Truth.assertThat;
import static org.aion.zero.impl.sync.TrieDatabase.DETAILS;
import static org.aion.zero.impl.sync.TrieDatabase.STATE;
import static org.aion.zero.impl.sync.TrieDatabase.STORAGE;
import static org.aion.zero.impl.sync.msg.ResponseTrieState.encodeReferencedNodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.aion.base.util.ByteArrayWrapper;
import org.aion.rlp.RLP;
import org.aion.zero.impl.sync.TrieDatabase;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for {@link ResponseTrieState} messages.
 *
 * @author Alexandra Roatis
 */
@RunWith(JUnitParamsRunner.class)
public class ResponseTrieStateTest {

    // keys
    private static final ByteArrayWrapper nodeKey =
            ByteArrayWrapper.wrap(
                    new byte[] {
                        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
                        22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32
                    });
    private static final ByteArrayWrapper altNodeKey =
            ByteArrayWrapper.wrap(
                    new byte[] {
                        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                        21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31
                    });
    private static final ByteArrayWrapper zeroNodeKey = ByteArrayWrapper.wrap(new byte[32]);
    private static final ByteArrayWrapper smallNodeKey =
            ByteArrayWrapper.wrap(
                    new byte[] {
                        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
                        22, 23, 24, 25, 26, 27, 28, 29, 30, 31
                    });
    private static final ByteArrayWrapper largeNodeKey =
            ByteArrayWrapper.wrap(
                    new byte[] {
                        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
                        22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33
                    });

    // values (taken from TrieTest#testGetReferencedTrieNodes_withStartFromAllNodes)
    private static final byte[] emptyValue = new byte[] {};
    private static final byte[] leafValue =
            new byte[] {
                -8, 114, -97, 60, -96, -3, -97, 10, 112, 111, 28, -32, 44, 18, 101, -106, 51, 6,
                -107, 0, 24, 13, 50, 81, -84, 68, 125, 110, 118, 97, -109, -96, -30, 107, -72, 80,
                -8, 78, -128, -118, -45, -62, 27, -50, -52, -19, -95, 0, 0, 0, -96, 69, -80, -49,
                -62, 32, -50, -20, 91, 124, 28, 98, -60, -44, 25, 61, 56, -28, -21, -92, -114, -120,
                21, 114, -100, -25, 95, -100, 10, -80, -28, -63, -64, -96, 14, 87, 81, -64, 38, -27,
                67, -78, -24, -85, 46, -80, 96, -103, -38, -95, -47, -27, -33, 71, 119, -113, 119,
                -121, -6, -85, 69, -51, -15, 47, -29, -88
            };
    private static final byte[] branchValue =
            new byte[] {
                -8, -111, -128, -96, -99, -57, 89, 41, -60, -8, -93, -128, 9, -59, -23, -116, 4, 70,
                -94, -76, 119, -16, 22, 117, -72, -96, -117, 125, -57, 95, 123, -29, -46, 37, -78,
                86, -96, 89, -62, -94, 108, -21, -48, -19, 80, 5, 59, -70, 24, 90, 125, 19, -31,
                -82, 88, 49, 78, 44, 55, -44, 108, 31, 123, -120, 95, -39, 59, 104, 122, -96, -102,
                -109, -7, 6, 55, -12, 8, 32, -45, 116, 125, -50, 117, 7, 6, -59, -109, 88, 32, 95,
                92, 97, 26, -107, -84, 85, 25, 9, 83, 78, -20, -37, -128, -128, -128, -128, -96,
                -35, -15, -76, -107, -93, -23, -114, 24, -105, -87, -79, 37, 125, 65, 114, -43, -97,
                -53, -32, -37, -94, 59, -117, -121, -127, 44, -94, -91, 89, 25, -39, -85, -128,
                -128, -128, -128, -128, -128, -128, -128
            };
    private static final byte[] extensionValue =
            new byte[] {
                -28, -126, 0, -96, -96, 47, 42, 45, 74, -35, -117, 16, -124, 44, -19, 49, -116, -10,
                -40, 41, -109, 34, -77, -58, -109, -116, -57, -110, 51, 7, 24, -70, 33, 120, 116,
                10, -106
            };

    // referenced nodes
    private static final Map<ByteArrayWrapper, byte[]> emptyReferences = Map.of();
    private static final Map<ByteArrayWrapper, byte[]> singleReference =
            Map.of(altNodeKey, branchValue);
    private static final Map<ByteArrayWrapper, byte[]> multipleReferences =
            Map.of(altNodeKey, branchValue, zeroNodeKey, extensionValue);

    // other
    private static final byte[] emptyByteArray = new byte[] {};

    /**
     * Parameters for testing:
     *
     * <ul>
     *   <li>{@link #testDecode_correct(ByteArrayWrapper, byte[], Map, TrieDatabase)}
     *   <li>{@link #testEncode_4Parameters_correct(ByteArrayWrapper, byte[], Map, TrieDatabase)}
     *   <li>{@link #testEncodeDecode(ByteArrayWrapper, byte[], Map, TrieDatabase)}
     * </ul>
     */
    @SuppressWarnings("unused")
    private Object correctParameters() {
        List<Object> parameters = new ArrayList<>();

        ByteArrayWrapper[] keyOptions = new ByteArrayWrapper[] {nodeKey, altNodeKey, zeroNodeKey};
        byte[][] valueOptions = new byte[][] {leafValue, branchValue, extensionValue};
        Object[] refOptions = new Object[] {emptyReferences, singleReference, multipleReferences};
        TrieDatabase[] dbOptions = new TrieDatabase[] {STATE, STORAGE, DETAILS};

        // network and directory
        String[] net_values = new String[] {"mainnet", "invalid"};
        for (ByteArrayWrapper key : keyOptions) {
            for (byte[] value : valueOptions) {
                for (Object refs : refOptions) {
                    for (TrieDatabase db : dbOptions) {
                        parameters.add(new Object[] {key, value, refs, db});
                    }
                }
            }
        }

        return parameters.toArray();
    }

    /**
     * Parameters for testing:
     *
     * <ul>
     *   <li>{@link #testEncode_3Parameters_correct(ByteArrayWrapper, byte[], TrieDatabase)}
     *   <li>{@link #testEncodeDecode_3Parameters(ByteArrayWrapper, byte[], TrieDatabase)}
     * </ul>
     */
    @SuppressWarnings("unused")
    private Object correct3Parameters() {
        List<Object> parameters = new ArrayList<>();

        ByteArrayWrapper[] keyOptions = new ByteArrayWrapper[] {nodeKey, altNodeKey, zeroNodeKey};
        byte[][] valueOptions = new byte[][] {leafValue, branchValue, extensionValue};
        TrieDatabase[] dbOptions = new TrieDatabase[] {STATE, STORAGE, DETAILS};

        // network and directory
        String[] net_values = new String[] {"mainnet", "invalid"};
        for (ByteArrayWrapper key : keyOptions) {
            for (byte[] value : valueOptions) {
                for (TrieDatabase db : dbOptions) {
                    parameters.add(new Object[] {key, value, db});
                }
            }
        }

        return parameters.toArray();
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor_3Parameters_nullKey() {
        new ResponseTrieState(null, leafValue, STATE);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor_4Parameters_nullKey() {
        new ResponseTrieState(null, leafValue, multipleReferences, STATE);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor_3Parameters_nullValue() {
        new ResponseTrieState(nodeKey, null, STATE);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor_4Parameters_nullValue() {
        new ResponseTrieState(nodeKey, null, multipleReferences, STATE);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor_4Parameters_nullReferencedNodes() {
        new ResponseTrieState(nodeKey, leafValue, null, STATE);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor_3Parameters_nullType() {
        new ResponseTrieState(nodeKey, leafValue, null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor_4Parameters_nullType() {
        new ResponseTrieState(nodeKey, leafValue, multipleReferences, null);
    }

    @Test
    public void testDecode_nullMessage() {
        assertThat(ResponseTrieState.decode(null)).isNull();
    }

    @Test
    public void testDecode_emptyMessage() {
        assertThat(ResponseTrieState.decode(emptyByteArray)).isNull();
    }

    @Test
    public void testDecode_missingKey() {
        byte[] encoding =
                RLP.encodeList(
                        RLP.encodeElement(leafValue),
                        RLP.encodeList(encodeReferencedNodes(multipleReferences)),
                        RLP.encodeString(STATE.toString()));
        assertThat(ResponseTrieState.decode(encoding)).isNull();
    }

    @Test
    public void testDecode_missingValue() {
        byte[] encoding =
                RLP.encodeList(
                        RLP.encodeElement(nodeKey.getData()),
                        RLP.encodeList(encodeReferencedNodes(multipleReferences)),
                        RLP.encodeString(STATE.toString()));
        assertThat(ResponseTrieState.decode(encoding)).isNull();
    }

    @Test
    public void testDecode_missingReferences() {
        byte[] encoding =
                RLP.encodeList(
                        RLP.encodeElement(nodeKey.getData()),
                        RLP.encodeElement(leafValue),
                        RLP.encodeString(STATE.toString()));
        assertThat(ResponseTrieState.decode(encoding)).isNull();
    }

    @Test
    public void testDecode_missingType() {
        byte[] encoding =
                RLP.encodeList(
                        RLP.encodeElement(nodeKey.getData()),
                        RLP.encodeElement(leafValue),
                        RLP.encodeList(encodeReferencedNodes(multipleReferences)));
        assertThat(ResponseTrieState.decode(encoding)).isNull();
    }

    @Test
    public void testDecode_additionalValue() {
        byte[] encoding =
                RLP.encodeList(
                        RLP.encodeElement(nodeKey.getData()),
                        RLP.encodeElement(nodeKey.getData()),
                        RLP.encodeElement(leafValue),
                        RLP.encodeList(encodeReferencedNodes(multipleReferences)),
                        RLP.encodeString(STATE.toString()));
        assertThat(ResponseTrieState.decode(encoding)).isNull();
    }

    @Test
    public void testDecode_outOfOrder() {
        byte[] encoding =
                RLP.encodeList(
                        RLP.encodeList(encodeReferencedNodes(multipleReferences)),
                        RLP.encodeElement(nodeKey.getData()),
                        RLP.encodeElement(leafValue),
                        RLP.encodeString(STATE.toString()));
        assertThat(ResponseTrieState.decode(encoding)).isNull();
    }

    @Test
    public void testDecode_smallerKeySize() {
        byte[] encoding =
                RLP.encodeList(
                        RLP.encodeElement(smallNodeKey.getData()),
                        RLP.encodeElement(leafValue),
                        RLP.encodeList(encodeReferencedNodes(multipleReferences)),
                        RLP.encodeString(STATE.toString()));
        assertThat(ResponseTrieState.decode(encoding)).isNull();
    }

    @Test
    public void testDecode_largerKeySize() {
        byte[] encoding =
                RLP.encodeList(
                        RLP.encodeElement(largeNodeKey.getData()),
                        RLP.encodeElement(leafValue),
                        RLP.encodeList(encodeReferencedNodes(multipleReferences)),
                        RLP.encodeString(STATE.toString()));
        assertThat(ResponseTrieState.decode(encoding)).isNull();
    }

    @Test
    public void testDecode_emptyValue() {
        byte[] encoding =
                RLP.encodeList(
                        RLP.encodeElement(nodeKey.getData()),
                        RLP.encodeElement(emptyValue),
                        RLP.encodeList(encodeReferencedNodes(multipleReferences)),
                        RLP.encodeString(STATE.toString()));
        assertThat(ResponseTrieState.decode(encoding)).isNull();
    }

    @Test
    public void testDecode_incorrectElementReferences() {
        byte[] encoding =
                RLP.encodeList(
                        RLP.encodeElement(nodeKey.getData()),
                        RLP.encodeElement(leafValue),
                        RLP.encodeElement(branchValue),
                        RLP.encodeString(STATE.toString()));
        assertThat(ResponseTrieState.decode(encoding)).isNull();
    }

    @Test
    public void testDecode_incorrectListReferences() {
        byte[] encoding =
                RLP.encodeList(
                        RLP.encodeElement(nodeKey.getData()),
                        RLP.encodeElement(leafValue),
                        RLP.encodeList(
                                RLP.encodeElement(nodeKey.getData()),
                                RLP.encodeElement(leafValue),
                                RLP.encodeElement(branchValue)),
                        RLP.encodeString(STATE.toString()));
        assertThat(ResponseTrieState.decode(encoding)).isNull();
    }

    @Test
    public void testDecode_incorrectPairReferences() {
        byte[] encoding =
                RLP.encodeList(
                        RLP.encodeElement(nodeKey.getData()),
                        RLP.encodeElement(leafValue),
                        RLP.encodeList(
                                RLP.encodeList(
                                        RLP.encodeElement(nodeKey.getData()),
                                        RLP.encodeElement(leafValue)),
                                branchValue),
                        RLP.encodeString(STATE.toString()));
        assertThat(ResponseTrieState.decode(encoding)).isNull();
    }

    @Test
    public void testDecode_incorrectValueReferences() {
        byte[] encoding =
                RLP.encodeList(
                        RLP.encodeElement(nodeKey.getData()),
                        RLP.encodeElement(leafValue),
                        RLP.encodeList(
                                RLP.encodeList(
                                        RLP.encodeElement(nodeKey.getData()),
                                        RLP.encodeElement(leafValue)),
                                RLP.encodeList(
                                        RLP.encodeElement(altNodeKey.getData()),
                                        RLP.encodeElement(emptyValue))),
                        RLP.encodeString(STATE.toString()));
        assertThat(ResponseTrieState.decode(encoding)).isNull();
    }

    @Test
    public void testDecode_incorrectKeyReferences() {
        byte[] encoding =
                RLP.encodeList(
                        RLP.encodeElement(nodeKey.getData()),
                        RLP.encodeElement(leafValue),
                        RLP.encodeList(
                                RLP.encodeList(
                                        RLP.encodeElement(nodeKey.getData()),
                                        RLP.encodeElement(leafValue)),
                                RLP.encodeList(
                                        RLP.encodeElement(smallNodeKey.getData()),
                                        RLP.encodeElement(branchValue))),
                        RLP.encodeString(STATE.toString()));
        assertThat(ResponseTrieState.decode(encoding)).isNull();
    }

    @Test
    public void testDecode_incorrectType() {
        byte[] encoding =
                RLP.encodeList(
                        RLP.encodeElement(nodeKey.getData()),
                        RLP.encodeElement(leafValue),
                        RLP.encodeList(encodeReferencedNodes(multipleReferences)),
                        RLP.encodeString("random"));
        assertThat(ResponseTrieState.decode(encoding)).isNull();
    }

    @Test
    @Parameters(method = "correctParameters")
    public void testDecode_correct(
            ByteArrayWrapper key,
            byte[] value,
            Map<ByteArrayWrapper, byte[]> referencedNodes,
            TrieDatabase dbType) {

        byte[] encoding =
                RLP.encodeList(
                        RLP.encodeElement(key.getData()),
                        RLP.encodeElement(value),
                        RLP.encodeList(encodeReferencedNodes(referencedNodes)),
                        RLP.encodeString(dbType.toString()));

        ResponseTrieState message = ResponseTrieState.decode(encoding);

        assertThat(message).isNotNull();
        assertThat(message.getNodeKey()).isEqualTo(key);
        assertThat(message.getNodeValue()).isEqualTo(value);
        assertThat(message.getReferencedNodes().size()).isEqualTo(referencedNodes.size());
        for (Map.Entry<ByteArrayWrapper, byte[]> entry : message.getReferencedNodes().entrySet()) {
            assertThat(Arrays.equals(referencedNodes.get(entry.getKey()), entry.getValue()))
                    .isTrue();
        }
        assertThat(message.getDbType()).isEqualTo(dbType);
    }

    @Test
    @Parameters(method = "correct3Parameters")
    public void testEncode_3Parameters_correct(
            ByteArrayWrapper key, byte[] value, TrieDatabase dbType) {

        byte[] expected =
                RLP.encodeList(
                        RLP.encodeElement(key.getData()),
                        RLP.encodeElement(value),
                        RLP.encodeList(encodeReferencedNodes(emptyReferences)),
                        RLP.encodeString(dbType.toString()));

        ResponseTrieState message = new ResponseTrieState(key, value, dbType);
        assertThat(message.encode()).isEqualTo(expected);
    }

    @Test
    @Parameters(method = "correctParameters")
    public void testEncode_4Parameters_correct(
            ByteArrayWrapper key,
            byte[] value,
            Map<ByteArrayWrapper, byte[]> referencedNodes,
            TrieDatabase dbType) {

        byte[] expected =
                RLP.encodeList(
                        RLP.encodeElement(key.getData()),
                        RLP.encodeElement(value),
                        RLP.encodeList(encodeReferencedNodes(referencedNodes)),
                        RLP.encodeString(dbType.toString()));

        ResponseTrieState message = new ResponseTrieState(key, value, referencedNodes, dbType);
        assertThat(message.encode()).isEqualTo(expected);
    }

    @Test
    @Parameters(method = "correctParameters")
    public void testEncodeDecode(
            ByteArrayWrapper key,
            byte[] value,
            Map<ByteArrayWrapper, byte[]> referencedNodes,
            TrieDatabase dbType) {

        // encode
        ResponseTrieState message = new ResponseTrieState(key, value, referencedNodes, dbType);

        assertThat(message.getNodeKey()).isEqualTo(key);
        assertThat(message.getNodeValue()).isEqualTo(value);
        assertThat(message.getReferencedNodes()).isEqualTo(referencedNodes);
        assertThat(message.getDbType()).isEqualTo(dbType);

        byte[] encoding = message.encode();

        // decode
        ResponseTrieState decoded = ResponseTrieState.decode(encoding);

        assertThat(decoded).isNotNull();

        assertThat(decoded.getNodeKey()).isEqualTo(key);
        assertThat(decoded.getNodeValue()).isEqualTo(value);
        assertThat(decoded.getReferencedNodes().size()).isEqualTo(referencedNodes.size());
        for (Map.Entry<ByteArrayWrapper, byte[]> entry : decoded.getReferencedNodes().entrySet()) {
            assertThat(Arrays.equals(referencedNodes.get(entry.getKey()), entry.getValue()))
                    .isTrue();
        }
        assertThat(decoded.getDbType()).isEqualTo(dbType);
    }

    @Test
    @Parameters(method = "correct3Parameters")
    public void testEncodeDecode_3Parameters(
            ByteArrayWrapper key, byte[] value, TrieDatabase dbType) {

        // encode
        ResponseTrieState message = new ResponseTrieState(key, value, dbType);

        assertThat(message.getNodeKey()).isEqualTo(key);
        assertThat(message.getNodeValue()).isEqualTo(value);
        assertThat(message.getReferencedNodes()).isEmpty();
        assertThat(message.getDbType()).isEqualTo(dbType);

        byte[] encoding = message.encode();

        // decode
        ResponseTrieState decoded = ResponseTrieState.decode(encoding);

        assertThat(decoded).isNotNull();

        assertThat(decoded.getNodeKey()).isEqualTo(key);
        assertThat(decoded.getNodeValue()).isEqualTo(value);
        assertThat(decoded.getReferencedNodes()).isEmpty();
        assertThat(decoded.getDbType()).isEqualTo(dbType);
    }

    @Test
    public void testEncode_differentKey() {
        byte[] encoding =
                RLP.encodeList(
                        RLP.encodeElement(nodeKey.getData()),
                        RLP.encodeElement(leafValue),
                        RLP.encodeList(encodeReferencedNodes(multipleReferences)),
                        RLP.encodeString(STATE.toString()));

        ResponseTrieState message =
                new ResponseTrieState(altNodeKey, leafValue, multipleReferences, STATE);
        assertThat(message.encode()).isNotEqualTo(encoding);
    }

    @Test
    public void testEncode_differentValue() {
        byte[] encoding =
                RLP.encodeList(
                        RLP.encodeElement(nodeKey.getData()),
                        RLP.encodeElement(leafValue),
                        RLP.encodeList(encodeReferencedNodes(multipleReferences)),
                        RLP.encodeString(STATE.toString()));

        ResponseTrieState message =
                new ResponseTrieState(nodeKey, branchValue, multipleReferences, STATE);
        assertThat(message.encode()).isNotEqualTo(encoding);
    }

    @Test
    public void testEncode_differentReferences() {
        byte[] encoding =
                RLP.encodeList(
                        RLP.encodeElement(nodeKey.getData()),
                        RLP.encodeElement(leafValue),
                        RLP.encodeList(encodeReferencedNodes(multipleReferences)),
                        RLP.encodeString(STATE.toString()));

        ResponseTrieState message =
                new ResponseTrieState(nodeKey, leafValue, singleReference, STATE);
        assertThat(message.encode()).isNotEqualTo(encoding);
    }

    @Test
    public void testEncode_differentType() {
        byte[] encoding =
                RLP.encodeList(
                        RLP.encodeElement(nodeKey.getData()),
                        RLP.encodeElement(leafValue),
                        RLP.encodeList(encodeReferencedNodes(multipleReferences)),
                        RLP.encodeString(STATE.toString()));

        ResponseTrieState message =
                new ResponseTrieState(nodeKey, leafValue, multipleReferences, STORAGE);
        assertThat(message.encode()).isNotEqualTo(encoding);
    }
}