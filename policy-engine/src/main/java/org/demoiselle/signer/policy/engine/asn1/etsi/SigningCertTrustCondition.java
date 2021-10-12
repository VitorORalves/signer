/*
 * Demoiselle Framework
 * Copyright (C) 2016 SERPRO
 * ----------------------------------------------------------------------------
 * This file is part of Demoiselle Framework.
 *
 * Demoiselle Framework is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this program; if not,  see <http://www.gnu.org/licenses/>
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA  02110-1301, USA.
 * ----------------------------------------------------------------------------
 * Este arquivo é parte do Framework Demoiselle.
 *
 * O Framework Demoiselle é um software livre; você pode redistribuí-lo e/ou
 * modificá-lo dentro dos termos da GNU LGPL versão 3 como publicada pela Fundação
 * do Software Livre (FSF).
 *
 * Este programa é distribuído na esperança que possa ser útil, mas SEM NENHUMA
 * GARANTIA; sem uma garantia implícita de ADEQUAÇÃO a qualquer MERCADO ou
 * APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU/LGPL em português
 * para maiores detalhes.
 *
 * Você deve ter recebido uma cópia da GNU LGPL versão 3, sob o título
 * "LICENCA.txt", junto com esse programa. Se não, acesse <http://www.gnu.org/licenses/>
 * ou escreva para a Fundação do Software Livre (FSF) Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02111-1301, USA.
 */

package org.demoiselle.signer.policy.engine.asn1.etsi;

import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.demoiselle.signer.policy.engine.asn1.ASN1Object;

/**
 * The SigningCertTrustCondition field identifies trust conditions
 * for certificate path processing used to validate the  signing
 * certificate.
 *
 * <pre>
 * SigningCertTrustCondition ::= SEQUENCE {
 *     signerTrustTrees {@link CertificateTrustTrees},
 *     signerRevReq {@link CertRevReq}
 * }
 * </pre>
 *
 * @see ASN1Sequence
 * @see ASN1Primitive
 */
public class SigningCertTrustCondition extends ASN1Object {

	private CertificateTrustTrees signerTrustTrees;
	private CertRevReq signerRevReq;

	public CertificateTrustTrees getSignerTrustTrees() {
		return signerTrustTrees;
	}

	public void setSignerTrustTrees(CertificateTrustTrees signerTrustTrees) {
		this.signerTrustTrees = signerTrustTrees;
	}

	public CertRevReq getSignerRevReq() {
		return signerRevReq;
	}

	public void setSignerRevReq(CertRevReq signerRevReq) {
		this.signerRevReq = signerRevReq;
	}

	@Override
	public void parse(ASN1Primitive derObject) {
		ASN1Sequence derSequence = ASN1Object.getDERSequence(derObject);
		this.signerTrustTrees = new CertificateTrustTrees();
		this.signerTrustTrees.parse(derSequence.getObjectAt(0).toASN1Primitive());
		this.signerRevReq = new CertRevReq();
		this.signerRevReq.parse(derSequence.getObjectAt(1).toASN1Primitive());
	}

}
